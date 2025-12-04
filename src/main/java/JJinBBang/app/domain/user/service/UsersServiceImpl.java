package JJinBBang.app.domain.user.service;

import JJinBBang.app.domain.building.dto.PageRequest;
import JJinBBang.app.domain.building.dto.UserReviewListResponse;
import JJinBBang.app.domain.building.dto.UserReviewResponse;
import JJinBBang.app.domain.building.entity.AgencyLikes;
import JJinBBang.app.domain.building.entity.BuildingLikes;
import JJinBBang.app.domain.building.entity.ReviewLikes;
import JJinBBang.app.domain.building.repository.*;
import JJinBBang.app.domain.common.entity.Universities;
import JJinBBang.app.domain.common.entity.ReportLikes;
import JJinBBang.app.domain.common.repository.ReportLikesRepository;
import JJinBBang.app.domain.user.dto.UserInfoResponseDto;
import JJinBBang.app.domain.user.exception.UniversityNotFoundException;
import JJinBBang.app.domain.user.repository.UniversityRepository;
import JJinBBang.app.global.common.enums.UnregisterReason;
import org.springframework.data.domain.Pageable;
import JJinBBang.app.global.common.enums.VerificationStatus;
import org.springframework.stereotype.Service;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.exception.UserNotFoundException;
import JJinBBang.app.domain.user.repository.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

	public static final String SYSTEM_DELETE_ID = "___system_delete_user___";

	private final UsersRepository usersRepository;
	private final GeneralReviewsRepository generalReviewsRepository;
	private final DormReviewsRepository dormReviewsRepository;
	private final AgencyReviewsRepository agencyReviewsRepository;
	private final ReviewLikesRepository reviewLikesRepository;
	private final ReviewDetailsRepository reviewDetailsRepository;
	private final UniversityRepository universityRepository;
	private final BuildingLikesRepository buildingLikesRepository;
	private final AgencyLikesRepository agencyLikesRepository;
	private final ReportLikesRepository reportLikesRepository;


	@Override
	public boolean existsByProviderId(String providerId) {
        return usersRepository.findByProviderId(providerId).isPresent();
    }

	@Override
	public Users findByProviderId(String providerId) {
		try {
			return usersRepository.findByProviderId(providerId).orElseThrow(UserNotFoundException::notFound);
		} catch (UserNotFoundException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw UserNotFoundException.searchFailed();
		}
	}

	@Override
	public Users save(Users user) {
		return usersRepository.save(user);
	}

	@Override
	public UserInfoResponseDto getUserInfo(Users user) {
		if (user == null) {
			throw UserNotFoundException.notFound();
		}
		return UserInfoResponseDto.of(user);
	}

	@Override
	public UserReviewListResponse getUserReviews(
			Users user, int offset, int limit, String order
	) {
		Long userId = user.getUserId();

		Pageable page = new PageRequest(offset, limit, null);

		var generalReviewsPage   = generalReviewsRepository.findByUser_UserId(userId, page);
		var dormReviewsPage  = dormReviewsRepository.findByUser_UserId(userId, page);
		var agencyReviewsPage = agencyReviewsRepository.findByUser_UserId(userId, page);

		Stream<UserReviewResponse> generalStream = generalReviewsPage.stream().map(
				generalReviews -> UserReviewResponse.fromGeneral(
						generalReviews,
						user,
						buildImageUrl(generalReviews.getId()),
						reviewLikesRepository,
						reviewDetailsRepository
				)
		);

		Stream<UserReviewResponse> dormStream = dormReviewsPage.stream().map(
				dormReviews -> UserReviewResponse.fromDormitory(
						dormReviews,
						user,
						buildImageUrl(dormReviews.getId()),
						reviewLikesRepository
				)
		);

		Stream<UserReviewResponse> agencyStream = agencyReviewsPage.stream().map(
				agencyReviews -> UserReviewResponse.fromAgency(
						agencyReviews,
						user,
						buildImageUrl(agencyReviews.getId()),
						reviewLikesRepository
				)
		);

		Comparator<UserReviewResponse> comparator = Comparator.comparing(r -> r.reviewInfo().updateAt());
		if ("latest".equalsIgnoreCase(order)) {
			comparator = comparator.reversed();
		}

		List<UserReviewResponse> paged = Stream.of(generalStream, dormStream, agencyStream)
				.flatMap(s -> s)
				.sorted(comparator)
				.skip(offset)
				.limit(limit)
				.toList();

		return new UserReviewListResponse(paged);
	}


	private String buildImageUrl(Long id) {
		return "http://localhost:8080/image/" + id + ".jpg";
	}

	@Override
	public Users verifyUniversityEmail(Users user, String universityEmail) {
		// 유저의 학교 이메일 업데이트
		user.updateUniversityEmail(universityEmail);
		// 유저의 인증 상태 업데이트
		user.updateVerificationStatus(VerificationStatus.EMAIL_VERIFIED);

		// 유저의 학교 정보 업데이트
		String domain = extractDomain(universityEmail);
		Universities authenticatedUniversity = universityRepository.findUniversitiesByUniversityDomain(domain)
				.orElseThrow(() -> UniversityNotFoundException.universityNotFound(domain));
		user.updateUniversity(authenticatedUniversity);

		return usersRepository.save(user);
	}

	@Override
	public Users findWithUniversity(String providerId) {
		return usersRepository.findWithUniversityByProviderId(providerId)
				.orElseThrow(UserNotFoundException::notFound);
	}

	@Override
	public Users findByUserId(Long userId) {
		return usersRepository.findByUserId(userId).orElseThrow(UserNotFoundException::notFound);
	}

	@Override
	@Transactional
	public void deleteUser(Users user) {
		if (user.getUserId() == 0L) {
			throw UserNotFoundException.systemError();
		}

		// 1) 시스템 유저 보장
		ensureSystemUserExists(user);

		Users systemUser = usersRepository.findByProviderId(SYSTEM_DELETE_ID)
			.orElseThrow(UserNotFoundException::systemError);

		Long targetUserId = user.getUserId();

		// 2) 작성 데이터(리뷰) → 시스템 유저로 재매핑 (벌크 업데이트)
		//  - 벌크 연산 후 1차 캐시와의 불일치 줄이려면 flush/clear 고려
		generalReviewsRepository.updateUserToSystemUser(targetUserId, systemUser);

		// 3) 행동 데이터(좋아요) → 삭제 + 카운터 보정
		// 3-1) 건물 좋아요
		List<BuildingLikes> buildingLikes = buildingLikesRepository.findAllByUser_UserId(targetUserId);
		buildingLikes.forEach(bl -> bl.getBuilding().decrementLikeCount());
		buildingLikesRepository.deleteAll(buildingLikes);

		// 3-2) 리뷰 좋아요
		List<ReviewLikes> reviewLikes = reviewLikesRepository.findAllByUser_UserId(targetUserId);
		reviewLikes.forEach(rl -> rl.getReview().decrementLikeCount());
		reviewLikesRepository.deleteAll(reviewLikes);

		// 3-3) 공인중개사 좋아요
		List<AgencyLikes> agencyLikes = agencyLikesRepository.findAllByUser_UserId(targetUserId);
		agencyLikes.forEach(al -> al.getAgency().decrementLikeCount());
		agencyLikesRepository.deleteAll(agencyLikes);

		// 3-4) 리포트 좋아요
		List<ReportLikes> reportLikes = reportLikesRepository.findAllByUser_UserId(targetUserId);
		reportLikes.forEach(rl -> rl.getReport().decreaseLikeCount());
		reportLikesRepository.deleteAllInBatch(reportLikes);

		// 3-x) Users 엔티티 컬렉션 정리 (양방향 연관관계 재-persist 방지)
		user.getBuildingLikes().clear();
		user.getReviewLikes().clear();
		user.getAgencyLikes().clear();
		user.getReportLikes().clear();

		// 4) 유저 탈퇴일 기록 (스케줄러가 N일 뒤 영구 삭제)
		user.delete(); // 내부에서 disabledAt = now 등
		usersRepository.save(user);
	}


	@Override
	@Transactional
	public void forceDeleteExecute(){
		List<Users> deletedUsers = usersRepository.findAllByDisabledAtIsNotNull();

		// 작성 데이터는 1단계에서 이미 재매핑되어 있으므로 여기선 유저만 삭제
		for (Users u : deletedUsers) {
			if (!u.getProviderId().equals(SYSTEM_DELETE_ID)) {
				usersRepository.delete(u);
			}
		}
	}

	private String extractDomain(String email) {
		int atIdx = email.lastIndexOf("@");
		return (atIdx != -1 && atIdx < email.length() - 1)
				? email.substring(atIdx + 1)
				: "";
	}

	private void ensureSystemUserExists(Users anyUserForFactory) {
		boolean exists = usersRepository.existsByProviderId(SYSTEM_DELETE_ID);
		if (!exists) {
			Users sys = anyUserForFactory.createDeletedSystemUser();
			usersRepository.saveAndFlush(sys); // 즉시 반영
		}
	}


	// 탈퇴 사유 문항 조회
	@Override
	public String optionToText(Integer option) {
		return UnregisterReason.fromNumber(option).getDescription();
	}
}
