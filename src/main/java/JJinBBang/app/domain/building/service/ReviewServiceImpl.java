package JJinBBang.app.domain.building.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Set;
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
	private final CampusesRepository campusesRepository;

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

        // 3) 각 리뷰를 ReviewSummaryResponse로 변환하여 반환
        return PaginatedResponse.of(
                reviewPage,
                review -> {
                    // 3.1) 좋아요 여부 확인
                    boolean liked = review.getReviewLikes().stream()
                            .anyMatch(like -> like.getUser().equals(user));

                    // 3.3) 요약 응답 생성
                    return ReviewSummaryResponse.of(review, liked);
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
                .anyMatch(like -> like.getUser().equals(user));

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
        if (!dto.imageUrls().isEmpty()) {
            building.incrementImagesCount();
        }

        // 4.4) 건물 유형 업데이트
				Set<BuildingType> newBuildingType = getBuildingTypesFromBuilding(building);
				building.updateBuildingType(newBuildingType);

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
        // 1) 캠퍼스 조회
        Campuses campus = campusesRepository.findById(dto.dormitoryReview().getCampusId())
                .orElseThrow(CampusNotFoundException::missingCampus);

        // 2) 건물 로드/생성
        Buildings building = findOrCreateBuilding(dto.buildingRequest(), campus);


        // 3) DormReviews 엔티티 저장
        DormReviews review = dto.toDormReviews(user, building);
        DormReviews saved = reviewsRepository.save(review);


        // 4) 시설 엔티티 리스트 생성 및 저장
        List<DormitoryFacilities> facilityList = convertToDormitoryFacilityList(dto.facilities(), saved);
        dormitoryFacilitiesRepository.saveAll(facilityList);


        // 5) ReviewDetails 엔티티 저장
        ReviewDetails details = dto.toReviewDetails(saved.getId(), building.getId());
        reviewDetailsRepository.save(details);

        // 6.1) 키워드 통계 증가
        updateKeywordCounts(building.getId(), false, Collections.emptyList(), dto.keywords().positive());

        // 6.2) 평점 반영
        building.addRating(saved.getRating());

        // 6.3) 이미지 카운트 반영
        if (!dto.imageUrls().isEmpty()) {
            building.incrementImagesCount();
        }

        // 6.4) 건물 엔티티 저장
        buildingsRepository.save(building);

        return saved.getId();
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
		if (!dto.imageUrls().isEmpty()) {
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
				// 1) 건물의 건물 유형 가져와서
				List<BuildingType> buildingtypeList = new ArrayList<>(existing.getBuildingType());

				// 2) 기존에 없는 건물 타입이면 추가
				if (!buildingtypeList.contains(dto.type())) {
					buildingtypeList.add(dto.type());
					existing.setBuildingType(buildingtypeList);
					buildingsRepository.save(existing);
				}
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
		return agenciesRepository.findByBuildingCode(dto.buildingCode())
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
			throw new UnsupportedOperationException("지원하지 않는 리뷰 타입입니다.");
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
			// a) 이전 건물: 평점 제거, 이미지 감소, 키워드 통계 감소
			oldBuilding.removeRating(oldReview.getRating());
			oldBuilding.decrementImagesCount();
			updateKeywordCounts(oldBuilding.getId(), false, oldDetails.getKeywords().positive(),
				Collections.emptyList());

			// b) 신규 건물: 평점 추가, 이미지 증가, 키워드 통계 증가
			newBuilding.addRating(dto.generalReview().getRating());
			newBuilding.incrementImagesCount();
			updateKeywordCounts(newBuilding.getId(), false, Collections.emptyList(), dto.keywords().positive());

			// c) 리뷰 엔티티 및 상세 정보 갱신 저장
			GeneralReviews updatedReview = dto.toUpdatedGeneralReviews(oldReview, newBuilding);
			reviewsRepository.save(updatedReview);
			ReviewDetails updatedDetails = dto.toUpdatedReviewDetails(oldDetails, newBuilding.getId());
			reviewDetailsRepository.save(updatedDetails);

			// d) 건물 유형 업데이트
			Set<BuildingType> newOldBuildingType = getBuildingTypesFromBuilding(oldBuilding);
			oldBuilding.updateBuildingType(newOldBuildingType);
			Set<BuildingType> newNewBuildingType = getBuildingTypesFromBuilding(oldBuilding);
			oldBuilding.updateBuildingType(newNewBuildingType);

			// c) 두 건물 저장
			buildingsRepository.saveAll(List.of(oldBuilding, newBuilding));
		} else {

			// a) 동일 건물: 평점 및 키워드 통계 업데이트
			oldBuilding.updateRating(oldReview.getRating(), dto.generalReview().getRating());
			updateKeywordCounts(oldBuilding.getId(), false, oldDetails.getKeywords().positive(),
				dto.keywords().positive());

			// b) 리뷰 엔티티 및 상세 정보 갱신 저장
			GeneralReviews updatedReview = dto.toUpdatedGeneralReviews(oldReview, newBuilding);
			reviewsRepository.save(updatedReview);
			ReviewDetails updatedDetails = dto.toUpdatedReviewDetails(oldDetails, newBuilding.getId());
			reviewDetailsRepository.save(updatedDetails);

			// c) 건물 유형 업데이트
			Set<BuildingType> newBuildingType = getBuildingTypesFromBuilding(oldBuilding);
			oldBuilding.updateBuildingType(newBuildingType);

			// d) 건물 저장
			buildingsRepository.save(oldBuilding);
		}
	}

	/**
	 * 기숙사 리뷰 업데이트 로직:
	 * 1) 캠퍼스 검증 및 조회
	 * 2) 기존/신규 건물 엔티티 로드
	 * 3) 기존 상세 정보 로드
	 * 4) 건물 변경 시 평점·이미지·키워드 이동, 동일 건물 시 업데이트
	 * 5) 리뷰 엔티티 갱신 저장
	 * 6) 기존 시설 삭제 후 재생성과 저장
	 * 7) 상세 정보 갱신 저장
	 */
	private void updateDormitoryReview(DormReviews oldReview, ReviewRequest dto) {
		// 1) 캠퍼스 검증
		Campuses campus = campusesRepository.findById(dto.dormitoryReview().getCampusId())
			.orElseThrow(CampusNotFoundException::missingCampus);

		// 2) 건물 엔티티 로드
		Buildings oldBuilding = oldReview.getBuilding();
		Buildings newBuilding = findOrCreateBuilding(dto.buildingRequest(), campus);

		// 3) 기존 상세 정보 로드
		ReviewDetails oldDetails = reviewDetailsRepository.findByReviewId(oldReview.getId())
			.orElseThrow(ReviewInternalServerErrorException::missingReviewDetailException);

		// 4) 건물 변경 처리
		if (!oldBuilding.getBuildingCode().equals(newBuilding.getBuildingCode())) {
			// a) 이전 건물 통계 감소
			oldBuilding.removeRating(oldReview.getRating());
			oldBuilding.decrementImagesCount();
			updateKeywordCounts(oldBuilding.getId(), false, oldDetails.getKeywords().positive(),
				Collections.emptyList());

			// b) 신규 건물 통계 증가
			newBuilding.addRating(dto.dormitoryReview().getRating());
			newBuilding.incrementImagesCount();
			updateKeywordCounts(newBuilding.getId(), false, Collections.emptyList(), dto.keywords().positive());
			buildingsRepository.saveAll(List.of(oldBuilding, newBuilding));
		} else {
			// 동일 건물: 평점·키워드 업데이트
			oldBuilding.updateRating(oldReview.getRating(), dto.dormitoryReview().getRating());
			updateKeywordCounts(oldBuilding.getId(), false, oldDetails.getKeywords().positive(),
				dto.keywords().positive());
			buildingsRepository.save(oldBuilding);
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
		// 3) 이미지 유무 확인
		boolean oldHasImage = oldReview.getThumbnailImage() != null;
		boolean newHasImage = dto.imageUrls() != null && !dto.imageUrls().isEmpty();

		// 4) 이미지 변경 및 공인중개사 변경 처리
		if (!oldAgency.getBuildingCode().equals(newAgency.getBuildingCode())) {
			if (!oldHasImage && newHasImage)
				newAgency.incrementImagesCount();
			else if (oldHasImage && !newHasImage)
				oldAgency.decrementImagesCount();
			else if (oldHasImage && newHasImage) {
				oldAgency.decrementImagesCount();
				newAgency.incrementImagesCount();
			}
			updateKeywordCounts(oldAgency.getAgencyId(), true, oldDetails.getKeywords().positive(),
				Collections.emptyList());
			updateKeywordCounts(newAgency.getAgencyId(), true, Collections.emptyList(), dto.keywords().positive());
			oldAgency.removeRating(oldReview.getRating());
			newAgency.addRating(dto.agencyReview().getRating());
			agenciesRepository.saveAll(List.of(oldAgency, newAgency));
		} else {
			if (!oldHasImage && newHasImage)
				oldAgency.incrementImagesCount();
			else if (oldHasImage && !newHasImage)
				oldAgency.decrementImagesCount();
			updateKeywordCounts(oldAgency.getAgencyId(), true, oldDetails.getKeywords().positive(),
				dto.keywords().positive());
			oldAgency.updateRating(oldReview.getRating(), dto.agencyReview().getRating());
			agenciesRepository.save(oldAgency);
		}

		// 5) 리뷰 및 상세 정보 저장
		AgencyReviews updatedReview = dto.toUpdatedAgencyReviews(oldReview, newAgency);
		reviewsRepository.save(updatedReview);
		ReviewDetails updatedDetails = dto.toUpdatedReviewDetails(oldDetails, newAgency.getAgencyId());
		reviewDetailsRepository.save(updatedDetails);
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
        // c)이미지 카운트 감소
        if (review.getThumbnailImage() != null) {
            building.decrementImagesCount();
        }
        // d)키워드 통계 감소
        updateKeywordCounts(building.getId(), false,
                detail.getKeywords().positive(), Collections.emptyList());

        // e)연관 데이터 삭제
        reviewDetailsRepository.deleteByReviewId(review.getId());
        reviewsRepository.delete(review);

		// f)건물 유형 업데이트
		Set<BuildingType> newBuildingType = getBuildingTypesFromBuilding(building);
		building.updateBuildingType(newBuildingType);

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
        if (review.getThumbnailImage() != null) {
            building.decrementImagesCount();
        }
        // 키워드 통계 감소
        updateKeywordCounts(building.getId(), false,
                detail.getKeywords().positive(), Collections.emptyList());
        buildingsRepository.save(building);

        // b) 연관된 시설, 상세정보, 리뷰 삭제
        dormitoryFacilitiesRepository.deleteAllByDormitoryReview(review);
        reviewDetailsRepository.deleteByReviewId(review.getId());
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
        if (review.getThumbnailImage() != null) {
            agency.decrementImagesCount();
        }
        // c) 키워드 통계 감소
        updateKeywordCounts(agency.getAgencyId(), true,
                detail.getKeywords().positive(), Collections.emptyList());
        agenciesRepository.save(agency);

        // d) 상세정보 및 리뷰 삭제
        reviewDetailsRepository.deleteByReviewId(review.getId());
        reviewsRepository.delete(review);
    }

	// 건물과 관련된 리뷰들의 건물 유형 집합 조회하는 메서드
	private Set<BuildingType> getBuildingTypesFromBuilding(Buildings building) {
		return reviewsRepository.findDistinctBuildingTypesByBuilding(building);
	}
}

