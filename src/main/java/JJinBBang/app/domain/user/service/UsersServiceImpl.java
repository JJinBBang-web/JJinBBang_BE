package JJinBBang.app.domain.user.service;

import JJinBBang.app.domain.building.dto.PageRequest;
import JJinBBang.app.domain.building.dto.UserReviewListResponse;
import JJinBBang.app.domain.building.dto.UserReviewResponse;
import JJinBBang.app.domain.building.repository.*;
import JJinBBang.app.domain.user.dto.UserInfoResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.exception.UserNotFoundException;
import JJinBBang.app.domain.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

	private final UsersRepository usersRepository;
	private final GeneralReviewsRepository generalReviewsRepository;
	private final DormReviewsRepository dormReviewsRepository;
	private final AgencyReviewsRepository agencyReviewsRepository;
	private final ReviewLikesRepository reviewLikesRepository;

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
						reviewLikesRepository
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
}
