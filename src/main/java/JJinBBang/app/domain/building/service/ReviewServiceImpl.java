package JJinBBang.app.domain.building.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import JJinBBang.app.domain.building.document.BuildingKeywordCounts;
import JJinBBang.app.domain.building.dto.*;
import JJinBBang.app.domain.building.entity.*;
import JJinBBang.app.domain.building.enums.*;
import JJinBBang.app.domain.building.exception.*;
import JJinBBang.app.domain.building.repository.*;
import JJinBBang.app.domain.common.dto.PaginatedResponse;
import JJinBBang.app.domain.common.entity.Campuses;
import JJinBBang.app.domain.common.repository.CampusesRepository;
import JJinBBang.app.domain.common.service.S3Service;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.common.enums.KeywordType;
import lombok.RequiredArgsConstructor;

/**
 * ReviewServiceImpl는 리뷰 생성, 조회, 수정 기능을 제공하는 서비스 구현체입니다.
 * - GENERAL, DORM, AGENCY 타입별 리뷰 처리
 * - 건물 및 공인중개사 엔티티 조회 또는 생성
 * - 평점, 이미지 카운트, 키워드 통계 업데이트
 */
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

	private final ReviewsRepository reviewsRepository;
	private final ReviewDetailsRepository reviewDetailsRepository;
	private final BuildingsRepository buildingsRepository;
	private final AgenciesRepository agenciesRepository;
	private final FacilitiesRepository facilitiesRepository;
	private final DormitoryFacilitiesRepository dormitoryFacilitiesRepository;
	private final BuildingKeywordCountsRepository buildingKeywordCountsRepository;
	private final S3Service s3Service;
	private final BuildingCodeResolver buildingCodeResolver;

	/**
     * 특정 건물 또는 공인중개사에 대한 리뷰 목록을 페이징 조회
     *
     * @param buildingId  건물 또는 공인중개사 ID
     * @param isAgency    true면 공인중개사, false면 건물
     * @param user        조회 요청 사용자(좋아요 여부 확인용)
     * @param pageRequest 페이징 및 정렬 정보
     * @return ReviewSummaryResponse 페이징 응답
     */
    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ReviewSummaryResponse> getReviewList(
            Long buildingId,
            Boolean isAgency,
            Users user,
            PageRequest pageRequest
    ) {
        // 1) 조회 대상 엔티티 로드
        Page<Reviews> reviewPage;
        if (isAgency) {
            Agencies agency = agenciesRepository.findById(buildingId)
                    .orElseThrow(BuildingNullException::new);  // 공인중개사 없으면 예외

            // 2) 공인중개사 리뷰 페치
            reviewPage = reviewsRepository.findAllByAgency(agency, pageRequest);
        } else {
            Buildings building = buildingsRepository.findById(buildingId)
                    .orElseThrow(BuildingNullException::new);  // 건물 없으면 예외

            // 2) 건물 리뷰 페치
            reviewPage = reviewsRepository.findAllByBuilding(building, pageRequest);
        }

		// 3-1. 리뷰 ID 목록 추출
		List<Long> reviewIds = reviewPage.getContent().stream()
			.map(Reviews::getId)
			.toList();

		// 3-2. 한 번의 쿼리로 모든 ReviewDetails 조회 후 Map으로 변환
		Map<Long, Integer> imageCountMap = reviewDetailsRepository.findAllByReviewIdIn(reviewIds).stream()
			.collect(Collectors.toMap(ReviewDetails::getReviewId, ReviewDetails::getImageCount));

		// 3-3. PaginatedResponse 생성 시 Map 사용
		return PaginatedResponse.of(
			reviewPage,
			review -> {
				boolean liked = review.getReviewLikes().stream()
					.anyMatch(like -> like.getUser() != null
						&& user != null
						&& like.getUser().getUserId().equals(user.getUserId()));

				Integer imageCount = imageCountMap.getOrDefault(review.getId(), 0);
				return ReviewSummaryResponse.of(review, liked, imageCount);
			}
		);
    }

    /**
     * 단일 리뷰 상세 조회
     *
     * @param reviewId 조회할 리뷰 ID
     * @param user     조회 요청 사용자(좋아요 여부 확인용)
     * @return ReviewDetailResponse 상세 응답
     */
    @Override
    @Transactional(readOnly = true)
    public ReviewDetailResponse getReviewDetail(Long reviewId, Users user) {
        // 1) 리뷰 엔티티 로드
        Reviews review = reviewsRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::missingReview);

        // 2) 좋아요 여부 확인
		boolean liked = review.getReviewLikes().stream()
			.anyMatch(like -> like.getUser() != null
				&& user != null
				&& like.getUser().getUserId().equals(user.getUserId()));

        // 3) 상세 정보 로드
        ReviewDetails detail = reviewDetailsRepository.findByReviewId(reviewId)
                .orElseThrow(ReviewInternalServerErrorException::missingReviewDetailException);

        // 4) 리뷰 타입에 따라 적절한 DTO로 변환
        if (review instanceof GeneralReviews general) {
            return ReviewDetailResponse.ofGeneral(general, detail, liked);
        } else if (review instanceof DormReviews dorm) {
            return ReviewDetailResponse.ofDormitory(dorm, detail, liked);
        } else if (review instanceof AgencyReviews agency) {
            return ReviewDetailResponse.ofAgency(agency, detail, liked);
        } else {
            // 지원하지 않는 타입이면 서버 오류
            throw ReviewInternalServerErrorException.notSupportReviewType();
        }
    }

    /**
     * 리뷰 생성 진입점
     *
     * @param reviewRequest DTO로 전달된 리뷰 생성 정보
     * @param user          작성자 엔티티
     * @param reviewType    리뷰 타입(GENERAL/DORM/AGENCY)
     * @return CreateReviewResponse 생성된 리뷰 ID
     */
    @Override
    @Transactional
    public CreateReviewResponse createReview(
            ReviewRequest reviewRequest,
            Users user,
            ReviewType reviewType
    ) {
        // 리뷰 타입에 따라 분기 처리
        if (reviewType == ReviewType.GENERAL) {
            return CreateReviewResponse.from(createGeneralReview(reviewRequest, user));
        } else if (reviewType == ReviewType.DORM) {
            return CreateReviewResponse.from(createDormitoryReview(reviewRequest, user));
        } else if (reviewType == ReviewType.AGENCY) {
            return CreateReviewResponse.from(createAgencyReview(reviewRequest, user));
        } else {
            // 미지원 타입 예외
            throw ReviewInternalServerErrorException.notSupportReviewType();
        }
    }

    /**
     * 일반 리뷰 생성 로직
     * 1) 건물 조회 또는 생성
     * 2) GeneralReviews 엔티티 저장
     * 3) ReviewDetails 저장
     * 4) 키워드 통계, 평점, 이미지 카운트 업데이트
     */
    private Long createGeneralReview(ReviewRequest dto, Users user) {
        // 1) 건물 로드/생성
        Buildings building = findOrCreateBuilding(dto.buildingRequest(), null);

        // 2) GeneralReviews 엔티티 변환 및 저장
        GeneralReviews review = dto.toGeneralReviews(user, building);
        GeneralReviews saved = reviewsRepository.save(review);

        // 3) ReviewDetails 엔티티 저장
        ReviewDetails details = dto.toReviewDetails(saved.getId(), building.getId());
        reviewDetailsRepository.save(details);

        // 4.1) 키워드 통계 증가
        updateKeywordCounts(
                building.getId(), false,
                Collections.emptyList(), dto.keywords().positive()
        );

        // 4.2) 평점 반영
        building.addRating(saved.getRating());

        // 4.3) 이미지 카운트 반영
		if (details.hasImages())
			building.incrementImagesCount();

        // 4.4) 건물 유형 업데이트
		building.addBuildingType(dto.buildingRequest().type());

		// 평균 보증금, 평균 관리비, 평균 월세, 평균 전세 정보 추가
		recalculateAndApplyBuildingAverages(building);

        // 4.5) 건물 엔티티 저장
        buildingsRepository.save(building);

        return saved.getId();
    }

    /**
     * 기숙사 리뷰 생성 로직
     * 1) 캠퍼스 조회
     * 2) 건물 조회 또는 생성
     * 3) DormReviews 저장
     * 4) DormitoryFacilities 목록 저장
     * 5) ReviewDetails 저장
     * 6) 키워드 통계, 평점, 이미지 업데이트
     */
    private Long createDormitoryReview(ReviewRequest dto, Users user) {
        // 건물 로드
        Buildings building = findAndValidateDormitoryById(dto.dormitoryReview().getDormitoryId());

		if (!building.getBuildingType().contains(BuildingType.DORMITORY)) {
			throw BuildingNotFoundException.missingDormitory();
		}

        // DormReviews 엔티티 저장
        DormReviews review = dto.toDormReviews(user, building);
        DormReviews saved = reviewsRepository.save(review);


        // 시설 엔티티 리스트 생성 및 저장
        List<DormitoryFacilities> facilityList = convertToDormitoryFacilityList(dto.facilities(), saved);
        dormitoryFacilitiesRepository.saveAll(facilityList);


        // ReviewDetails 엔티티 저장
        ReviewDetails details = dto.toReviewDetails(saved.getId(), building.getId());
        reviewDetailsRepository.save(details);

        // 키워드 통계 증가
        updateKeywordCounts(building.getId(), false, Collections.emptyList(), dto.keywords().positive());

        // 평점 반영
        building.addRating(saved.getRating());

        // 이미지 카운트 반영
		if (details.hasImages()) {
			building.incrementImagesCount();
		}

        // 건물 엔티티 저장
        buildingsRepository.save(building);

        return saved.getId();
    }

	private Buildings findOrCreateDormitory(BuildingRequest buildingRequest, Campuses campus) {
		// 카카오 장소 ID를 건물관리번호로 변환
		String resolvedCode = buildingCodeResolver.resolve(
			buildingRequest.longitude(),
			buildingRequest.latitude(),
			buildingRequest.buildingCode()
		);

		BuildingRequest updatedRequest = buildingRequest.updateBuildingCode(resolvedCode);

		return findOrCreateBuilding(updatedRequest, campus);
	}

	private Buildings findAndValidateDormitoryById(Long dormitoryId) {
		Buildings building = buildingsRepository.findById(dormitoryId)
				.orElseThrow(BuildingNotFoundException::missingDormitory);
		if (!building.getBuildingType().contains(BuildingType.DORMITORY)) {
			throw BuildingNotFoundException.missingDormitory();
		}
		return building;
	}

	/**
     * 공인중개사 리뷰 생성 로직
     * 1) 공인중개사 조회 또는 생성
     * 2) AgencyReviews 저장
     * 3) ReviewDetails 저장
     * 4) 키워드 통계, 평점, 이미지 업데이트
     */
    private Long createAgencyReview(ReviewRequest dto, Users user) {
        // 1) 공인중개사 로드/생성
        Agencies agency = findOrCreateAgency(dto.buildingRequest());

        // 2) AgencyReviews 엔티티 저장
        AgencyReviews review = dto.toAgencyReviews(user, agency);
        AgencyReviews saved = reviewsRepository.save(review);

		// 3) ReviewDetails 엔티티 저장
		ReviewDetails details = dto.toReviewDetails(saved.getId(), agency.getAgencyId());
		reviewDetailsRepository.save(details);

		// 4.1) 키워드 통계 증가
		updateKeywordCounts(agency.getAgencyId(), true, Collections.emptyList(), dto.keywords().positive());

		// 4.2) 평점 반영
		agency.addRating(saved.getRating());

		// 4.3) 이미지 카운트 반영
		if (details.hasImages()) {
			agency.incrementImagesCount();
		}

		// 4.4) 공인중개사 엔티티 저장
		agenciesRepository.save(agency);

		return saved.getId();
	}

	/**
	 * 건물 조회 또는 신규 생성 헬퍼
	 *
	 * @param dto    BuildingRequest DTO
	 * @param campus 소속 캠퍼스(기숙사 리뷰용)
	 * @return Buildings 엔티티
	 */
	private Buildings findOrCreateBuilding(BuildingRequest dto, Campuses campus) {
		return buildingsRepository.findByBuildingCode(dto.buildingCode())
			// 기존에 있는 건물인 경우,
			.map(existing -> {
				existing.addBuildingType(dto.type());
				buildingsRepository.save(existing);
				return existing;
			})

			// 기존에 없는 건물은 새로 생성
			.orElseGet(() -> {
				Buildings saved = buildingsRepository.save(dto.toBuildingEntity(campus));
				BuildingKeywordCounts counts = BuildingKeywordCounts.of(saved.getId(), false);
				buildingKeywordCountsRepository.save(counts);
				return saved;
			});
	}

	/**
	 * 공인중개사 조회 또는 신규 생성 헬퍼
	 * @param dto BuildingRequest DTO
	 * @return Agencies 엔티티
	 */
	private Agencies findOrCreateAgency(BuildingRequest dto) {
		return agenciesRepository.findByAgencySerial(dto.buildingCode())
			.orElseGet(() -> {
				// 신규 공인중개사 생성 및 초기 키워드 통계 생성
				Agencies savedAgency = agenciesRepository.save(dto.toAgencyEntity());
				BuildingKeywordCounts counts = BuildingKeywordCounts.of(
					savedAgency.getAgencyId(), true);
				buildingKeywordCountsRepository.save(counts);
				return savedAgency;
			});
	}

	/**
	 * 키워드 통계 업데이트 헬퍼
	 * @param buildingId 건물 또는 공인중개사 ID
	 * @param isAgency true=공인중개사, false=건물
	 * @param oldPos 이전 긍정 키워드 목록
	 * @param newPos 신규 긍정 키워드 목록
	 */
	private void updateKeywordCounts(
		Long buildingId,
		Boolean isAgency,
		List<KeywordType> oldPos,
		List<KeywordType> newPos
	) {
		// 1) BuildingKeywordCounts 조회 또는 신규 생성
		BuildingKeywordCounts counts = buildingKeywordCountsRepository
			.findByBuildingIdAndIsAgency(buildingId, isAgency)
			.orElseGet(() -> BuildingKeywordCounts.of(buildingId, isAgency));

		// 2) 이전 키워드 감소
		counts.decrementPositiveKeywords(oldPos == null ? Collections.emptyList() : oldPos);

		// 3) 신규 키워드 증가
		counts.incrementPositiveKeywords(newPos);

		// 4) 저장
		buildingKeywordCountsRepository.save(counts);
	}

	/**
	 * 기숙사 시설 변환 헬퍼
	 * @param dto FacilitiesDto
	 * @param review DormReviews 엔티티
	 * @return DormitoryFacilities 리스트
	 */
	private List<DormitoryFacilities> convertToDormitoryFacilityList(FacilitiesDto dto, DormReviews review) {
		List<DormitoryFacilities> entities = new ArrayList<>();

		// public 시설들
		for (String name : dto.publicFacilities()) {
			Facilities facility = facilitiesRepository.findByName(name)
				.orElseThrow(BuildingNotFoundException::unsupportedDormitoryFacility);
			entities.add(DormitoryFacilities.create(review, facility, true, UsageType.PUBLIC));
		}

		// private 시설들
		for (String name : dto.privateFacilities()) {
			Facilities facility = facilitiesRepository.findByName(name)
				.orElseThrow(BuildingNotFoundException::unsupportedDormitoryFacility);
			entities.add(DormitoryFacilities.create(review, facility, true, UsageType.PRIVATE));
		}

		// 휴게시설 추가
		Facilities lounge = facilitiesRepository.findByName("lounge")
			.orElseThrow(BuildingNotFoundException::unsupportedDormitoryFacility);
		entities.add(DormitoryFacilities.create(review, lounge, dto.lounge(), null));

		return entities;
	}

	/**
	 * 리뷰 수정 진입점
	 * @param dto 업데이트할 리뷰 정보
	 * @param user 수정 요청 사용자
	 * @param reviewId 수정 대상 리뷰 ID
	 */
	@Override
	@Transactional
	public void updateReview(ReviewRequest dto, Users user, Long reviewId) {
		// 1) 리뷰 로드 및 존재 확인
		Reviews review = reviewsRepository.findById(reviewId)
			.orElseThrow(ReviewNotFoundException::missingReview);

		// 2) 작성자 권한 확인
		if (!review.getUser().getUserId().equals(user.getUserId())) {
			throw ReviewAccessDeniedException.onlyAuthorCanEdit();
		}

		// 3) 리뷰 타입별 분기 처리
		if (review instanceof GeneralReviews general) {
			updateGeneralReview(general, dto);
		} else if (review instanceof DormReviews dorm) {
			updateDormitoryReview(dorm, dto);
		} else if (review instanceof AgencyReviews agency) {
			updateAgencyReview(agency, dto);
		} else {
			throw ReviewNotFoundException.missingReviewType();
		}
	}

	/**
	 * 일반 리뷰 업데이트 로직:
	 * 1) 기존/신규 건물 로드
	 * 2) 기존 상세 정보 로드
	 * 3) 건물 변경 시 평점·이미지·키워드 이동, 동일 건물 시 업데이트
	 * 4) 리뷰 엔티티 및 상세 정보 저장
	 */
	private void updateGeneralReview(GeneralReviews oldReview, ReviewRequest dto) {
		// 1) 기존/신규 건물 엔티티 준비
		Buildings oldBuilding = oldReview.getBuilding();
		Buildings newBuilding = findOrCreateBuilding(dto.buildingRequest(), null);
		ReviewDetails oldDetails = reviewDetailsRepository.findByReviewId(oldReview.getId())
			.orElseThrow(ReviewInternalServerErrorException::missingReviewDetailException);

		// 2) 건물 변경 여부 판단
		if (!oldBuilding.getBuildingCode().equals(newBuilding.getBuildingCode())) {
			// a) 이전 건물: 평점 제거, 키워드 통계 감소
			oldBuilding.removeRating(oldReview.getRating());
			updateKeywordCounts(oldBuilding.getId(), false, oldDetails.getKeywords().positive(),
				Collections.emptyList());

			// b) 신규 건물: 평점 추가, 키워드 통계 증가
			newBuilding.addRating(dto.generalReview().getRating());
			updateKeywordCounts(newBuilding.getId(), false, Collections.emptyList(), dto.keywords().positive());

			// c) 리뷰 엔티티 및 상세 정보 갱신 저장
			GeneralReviews updatedReview = dto.toUpdatedGeneralReviews(oldReview, newBuilding);
			reviewsRepository.save(updatedReview);
			ReviewDetails updatedDetails = dto.toUpdatedReviewDetails(oldDetails, newBuilding.getId());
			reviewDetailsRepository.save(updatedDetails);

			// 이미지 변경 처리
			if (oldDetails.hasImages()) oldBuilding.decrementImagesCount();
			if (updatedDetails.hasImages()) newBuilding.incrementImagesCount();

			// 삭제할 이미지 삭제
			deleteRemovedImages(oldDetails.getImages(), updatedDetails.getImages());

			// d) 건물 유형 업데이트
			newBuilding.addBuildingType(dto.buildingRequest().type());

			// 이전 건물 평균 보증금, 평균 관리비, 평균 월세, 평균 전세 정보 삭제
			recalculateAndApplyBuildingAverages(oldBuilding);
			// 신규 건물 평균 보증금, 평균 관리비, 평균 월세, 평균 전세 정보 추가
			recalculateAndApplyBuildingAverages(newBuilding);			// 이전 건물 월세인 경우 평균 보증금, 평균 관리비, 평균 월세 정보 삭제

			// c) 두 건물 저장
			buildingsRepository.saveAll(List.of(oldBuilding, newBuilding));
		} else {

			// a) 동일 건물: 평점 및 키워드 통계 업데이트
			oldBuilding.updateRating(oldReview.getRating(), dto.generalReview().getRating());
			updateKeywordCounts(oldBuilding.getId(), false, oldDetails.getKeywords().positive(),
				dto.keywords().positive());

			// b) 리뷰 엔티티 및 상세 정보 갱신 저장
			GeneralReviews updatedReview = dto.toUpdatedGeneralReviews(oldReview, oldBuilding);
			reviewsRepository.save(updatedReview);
			ReviewDetails updatedDetails = dto.toUpdatedReviewDetails(oldDetails, oldBuilding.getId());
			reviewDetailsRepository.save(updatedDetails);

			// 이미지 증감 반영
			boolean oldHas = oldDetails.hasImages();
			boolean nowHas = updatedDetails.hasImages();
			if (!oldHas && nowHas) oldBuilding.incrementImagesCount();
			else if (oldHas && !nowHas) oldBuilding.decrementImagesCount();

			// 삭제할 이미지 삭제
			deleteRemovedImages(oldDetails.getImages(), updatedDetails.getImages());

			// c) 건물 유형 업데이트
			oldBuilding.addBuildingType(dto.buildingRequest().type());

			// 평균 보증금, 평균 관리비, 평균 월세, 평균 전세 정보 추가
			recalculateAndApplyBuildingAverages(oldBuilding);

			// d) 건물 저장
			buildingsRepository.save(oldBuilding);
		}
	}

	/**
	 * 기숙사 리뷰 업데이트 로직
	 * 1) 캠퍼스 검증 및 조회
	 * 2) 기존/신규 건물 엔티티 로드
	 * 3) 기존 상세 정보 로드
	 * 4) 건물 변경 여부 판단(moved)
	 *    - moved = true  : 이전 건물 평점/키워드 감소, 신규 건물 평점/키워드 증가
	 *    - moved = false : 동일 건물 평점/키워드 갱신
	 * 5) 리뷰 엔티티 및 상세 정보 갱신 저장
	 * 6) 시설 정보 재설정(기존 삭제 후 요청 기준으로 재생성)
	 * 7) 이미지 변경 사항 비교 후 이미지 카운트 정확 조정 (null-safe)
	 *    - moved = true  : oldHas면 이전 건물 decrement, nowHas면 신규 건물 increment
	 *    - moved = false : (없→있) increment, (있→없) decrement
	 * 8) 삭제된 이미지 S3 정리(old - new 차집합 삭제)
	 */
	private void updateDormitoryReview(DormReviews oldReview, ReviewRequest dto) {
		//  건물 엔티티 로드
		Buildings oldBuilding = oldReview.getBuilding();
		Buildings newBuilding = findAndValidateDormitoryById(dto.dormitoryReview().getDormitoryId());

		//  기존 상세 정보 로드
		ReviewDetails oldDetails = reviewDetailsRepository.findByReviewId(oldReview.getId())
			.orElseThrow(ReviewInternalServerErrorException::missingReviewDetailException);

		boolean moved = !oldBuilding.getBuildingCode().equals(newBuilding.getBuildingCode());

		if (moved) {
			// a) 이전 건물 통계 감소(평점/키워드) ― 이미지 카운트는 나중에 old/new 비교로 처리
			oldBuilding.removeRating(oldReview.getRating());
			updateKeywordCounts(oldBuilding.getId(), false, oldDetails.getKeywords().positive(), Collections.emptyList());

			// b) 신규 건물 통계 증가(평점/키워드)
			newBuilding.addRating(dto.dormitoryReview().getRating());
			updateKeywordCounts(newBuilding.getId(), false, Collections.emptyList(), dto.keywords().positive());
		} else {
			// 동일 건물: 평점·키워드만 선반영
			oldBuilding.updateRating(oldReview.getRating(), dto.dormitoryReview().getRating());
			updateKeywordCounts(oldBuilding.getId(), false, oldDetails.getKeywords().positive(), dto.keywords().positive());
		}

		// 5) 리뷰 엔티티 갱신 저장
		DormReviews updatedReview = dto.toUpdatedDormitoryReviews(oldReview, newBuilding);
		reviewsRepository.save(updatedReview);

		// 6) 시설 정보 재설정
		dormitoryFacilitiesRepository.deleteAllByDormitoryReview(oldReview);
		List<DormitoryFacilities> newFacilities = convertToDormitoryFacilityList(dto.facilities(), updatedReview);
		dormitoryFacilitiesRepository.saveAll(newFacilities);

		// 7) 상세 정보 갱신 저장
		ReviewDetails updatedDetails = dto.toUpdatedReviewDetails(oldDetails, newBuilding.getId());
		reviewDetailsRepository.save(updatedDetails);

		boolean oldHas = oldDetails.getImages() != null && !oldDetails.getImages().isEmpty();
		boolean nowHas = updatedDetails.getImages() != null && !updatedDetails.getImages().isEmpty();

		if (moved) {
			if (oldHas) oldBuilding.decrementImagesCount();
			if (nowHas) newBuilding.incrementImagesCount();
			buildingsRepository.saveAll(List.of(oldBuilding, newBuilding));
		} else {
			if (!oldHas && nowHas) {
				oldBuilding.incrementImagesCount();
			} else if (oldHas && !nowHas) {
				oldBuilding.decrementImagesCount();
			}
			buildingsRepository.save(oldBuilding);
		}

		// 삭제할 이미지 실제 삭제 (old - new)
		deleteRemovedImages(oldDetails.getImages(), updatedDetails.getImages());
	}

	/**
	 * 공인중개사 리뷰 업데이트 로직:
	 * 1) 기존/신규 공인중개사 로드
	 * 2) 기존 상세 정보 로드
	 * 3) 이미지 변경 여부에 따른 카운트 조정
	 * 4) 공인중개사 변경 시 평점·키워드 이동, 동일 시 업데이트
	 * 5) 리뷰 엔티티 및 상세 정보 저장
	 */
	private void updateAgencyReview(AgencyReviews oldReview, ReviewRequest dto) {
		// 1) 공인중개사 로드
		Agencies oldAgency = oldReview.getAgency();
		Agencies newAgency = findOrCreateAgency(dto.buildingRequest());
		// 2) 기존 상세 정보 로드
		ReviewDetails oldDetails = reviewDetailsRepository.findByReviewId(oldReview.getId())
			.orElseThrow(ReviewInternalServerErrorException::missingReviewDetailException);

		// 리뷰/상세 저장
		AgencyReviews updatedReview = dto.toUpdatedAgencyReviews(oldReview, newAgency);
		reviewsRepository.save(updatedReview);
		ReviewDetails updatedDetails = dto.toUpdatedReviewDetails(oldDetails, newAgency.getAgencyId());
		reviewDetailsRepository.save(updatedDetails);

		deleteRemovedImages(oldDetails.getImages(), updatedDetails.getImages());

		// 3) 이미지 유무 확인
		boolean oldHas = oldDetails.getImages() != null && !oldDetails.getImages().isEmpty();
		boolean nowHas = updatedDetails.getImages() != null && !updatedDetails.getImages().isEmpty();

		// 4) 이미지 변경 및 공인중개사 변경 처리
		if (!oldAgency.getAgencySerial().equals(newAgency.getAgencySerial())) {
			if (oldHas)
				oldAgency.decrementImagesCount();
			if (nowHas)
				newAgency.incrementImagesCount();

			updateKeywordCounts(oldAgency.getAgencyId(), true, oldDetails.getKeywords().positive(),
				Collections.emptyList());
			updateKeywordCounts(newAgency.getAgencyId(), true, Collections.emptyList(), dto.keywords().positive());
			oldAgency.removeRating(oldReview.getRating());
			newAgency.addRating(dto.agencyReview().getRating());
			agenciesRepository.saveAll(List.of(oldAgency, newAgency));
		} else {
			if (!oldHas && nowHas) {
				oldAgency.incrementImagesCount();
			} else if (oldHas && !nowHas) {
				oldAgency.decrementImagesCount();
			}

			updateKeywordCounts(oldAgency.getAgencyId(), true, oldDetails.getKeywords().positive(),
				dto.keywords().positive());
			oldAgency.updateRating(oldReview.getRating(), dto.agencyReview().getRating());
			agenciesRepository.save(oldAgency);
		}

	}

    /**
     * 리뷰 수정 진입점
     *
     * @param user     삭제 요청 사용자
     * @param reviewId 삭제 대상 리뷰 ID
     */
    @Override
    @Transactional
    public void deleteReview(Users user, Long reviewId) {
        // 1) 리뷰 로드 및 존재 확인
        Reviews review = reviewsRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::missingReview);

        // 2) 작성자 권한 확인
        if (review.getUser() != null && !review.getUser().getUserId().equals(user.getUserId())) {
            throw ReviewAccessDeniedException.onlyAuthorCanDelete();
        }

        // 3) 타입별 삭제 로직 분기
        if (review instanceof GeneralReviews general) {
            deleteGeneralReview(general);
        } else if (review instanceof DormReviews dorm) {
            deleteDormitoryReview(dorm);
        } else if (review instanceof AgencyReviews agency) {
            deleteAgencyReview(agency);
        } else {
            throw ReviewInternalServerErrorException.notSupportReviewType();
        }
    }

    /**
     * 일반 리뷰 삭제 로직:
     * 1) 통계 반영: 평점 제거, 이미지 카운트 감소, 키워드 통계 감소
     * 2) 연관 데이터 삭제: 상세 정보, 리뷰 엔티티 삭제
     *
     * @param review 삭제할 GeneralReviews 엔티티
     */
    private void deleteGeneralReview(GeneralReviews review) {
        // a) 통계 반영
        Buildings building = review.getBuilding();
        ReviewDetails detail = reviewDetailsRepository.findByReviewId(review.getId())
                .orElseThrow(ReviewInternalServerErrorException::missingReviewDetailException);

        // b)평점 제거
        building.removeRating(review.getRating());

   		if (detail.hasImages()) {
			building.decrementImagesCount();
		}

        // d)키워드 통계 감소
        updateKeywordCounts(building.getId(), false,
                detail.getKeywords().positive(), Collections.emptyList());

		// 리뷰 데이터 삭제
		if (detail.hasImages()) {
			detail.getImages().forEach(s3Service::deleteFile);
		}

		// e)연관 데이터 삭제
		reviewDetailsRepository.delete(detail);
        reviewsRepository.delete(review);

		// 평균 보증금, 평균 관리비, 평균 월세, 평균 전세 정보 추가
		recalculateAndApplyBuildingAverages(building);

        buildingsRepository.save(building);
    }

    /**
     * 기숙사 리뷰 삭제 로직:
     * 1) 통계 반영: 평점 제거, 이미지 카운트 감소, 키워드 통계 감소
     * 2) 연관된 시설, 상세 정보, 리뷰 엔티티 삭제
     *
     * @param review 삭제할 DormReviews 엔티티
     */
    private void deleteDormitoryReview(DormReviews review) {
        // a) 통계 반영
        Buildings building = review.getBuilding();
        ReviewDetails detail = reviewDetailsRepository.findByReviewId(review.getId())
                .orElseThrow(ReviewInternalServerErrorException::missingReviewDetailException);

        // 평점 제거
        building.removeRating(review.getRating());
        // 이미지 카운트 감소
		if (detail.hasImages())
			building.decrementImagesCount();

        // 키워드 통계 감소
        updateKeywordCounts(building.getId(), false,
                detail.getKeywords().positive(), Collections.emptyList());
        buildingsRepository.save(building);

        // b) 연관된 시설, 상세정보, 리뷰 삭제
        dormitoryFacilitiesRepository.deleteAllByDormitoryReview(review);

		// 리뷰 데이터 삭제
		if (detail.hasImages())
			detail.getImages().forEach(s3Service::deleteFile);

		// e)연관 데이터 삭제
		reviewDetailsRepository.delete(detail);

        reviewsRepository.delete(review);
    }

    /**
     * 공인중개사 리뷰 삭제 로직:
     * 1) 통계 반영: 평점 제거, 이미지 카운트 감소, 키워드 통계 감소
     * 2) 연관 상세 정보 및 리뷰 엔티티 삭제
     *
     * @param review 삭제할 AgencyReviews 엔티티
     */
    private void deleteAgencyReview(AgencyReviews review) {
        // a) 통계 반영
        Agencies agency = review.getAgency();
        ReviewDetails detail = reviewDetailsRepository.findByReviewId(review.getId())
                .orElseThrow(ReviewInternalServerErrorException::missingReviewDetailException);

        // b) 평점 제거
        agency.removeRating(review.getRating());
        // 이미지 카운트 감소
		if (detail.hasImages()) {
			agency.decrementImagesCount();
		}

        // c) 키워드 통계 감소
        updateKeywordCounts(agency.getAgencyId(), true,
                detail.getKeywords().positive(), Collections.emptyList());
        agenciesRepository.save(agency);

		// 리뷰 데이터 삭제
		if (detail.hasImages()) {
			detail.getImages().forEach(s3Service::deleteFile);
		}

		// e)연관 데이터 삭제
		reviewDetailsRepository.delete(detail);
        reviewsRepository.delete(review);
    }

	// 건물의 평균 금액(월세/전세/관리비)을 재계산하여 적용
	private void recalculateAndApplyBuildingAverages(Buildings building) {
		List<GeneralReviews> list = reviewsRepository.findAllByBuilding(building).stream()
			.filter(r -> r instanceof GeneralReviews)
			.map(r -> (GeneralReviews)r)
			.toList();

		long sumDeposit = 0L, sumMonthly = 0L, sumMaint = 0L, sumJeonse = 0L;
		int cntDeposit = 0, cntMonthly = 0, cntMaint = 0, cntJeonse = 0;

		for (GeneralReviews r : list) {
			// 관리비: 월세/전세 공통
			Integer maintenanceCost = r.getMaintenanceCost();
			if (maintenanceCost != null) {
				sumMaint += maintenanceCost;
				cntMaint++;
			}

			ContractType type = r.getContractType();
			if (type == ContractType.MONTHLY_RENT) {
				Integer monthly = r.getPrice();    // 월세
				if (monthly != null) {
					sumMonthly += monthly;
					cntMonthly++;
				}
				Integer deposit = r.getDeposit();  // 보증금
				if (deposit != null) {
					sumDeposit += deposit;
					cntDeposit++;
				}
			} else {
				Integer jeonse = r.getDeposit();     // 전세 보증금
				if (jeonse != null) {
					sumJeonse += jeonse;
					cntJeonse++;
				}
			}
		}

		Long avgDeposit = (cntDeposit > 0) ? Math.round((double)sumDeposit / cntDeposit) : null;
		Long avgMonthlyRent = (cntMonthly > 0) ? Math.round((double)sumMonthly / cntMonthly) : null;
		Long avgMaintenance = (cntMaint > 0) ? Math.round((double)sumMaint / cntMaint) : null;
		Long avgRentDeposit = (cntJeonse > 0) ? Math.round((double)sumJeonse / cntJeonse) : null;

		building.applyAverages(avgDeposit, avgMaintenance, avgMonthlyRent, avgRentDeposit);
	}

	// null-safe로 상세 이미지 삭제 (old - new 차집합)
	private void deleteRemovedImages(List<String> oldImages, List<String> newImages) {
		List<String> safeOld = (oldImages == null) ? Collections.emptyList() : oldImages;
		List<String> safeNew = (newImages == null) ? Collections.emptyList() : newImages;

		List<String> imagesToDelete = new ArrayList<>(safeOld);
		imagesToDelete.removeAll(safeNew);
		imagesToDelete.forEach(s3Service::deleteFile);
	}
}
