package JJinBBang.app.domain.building.service;

import java.util.ArrayList;
import java.util.List;

import JJinBBang.app.domain.building.dto.*;
import JJinBBang.app.domain.building.exception.*;
import JJinBBang.app.domain.building.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import JJinBBang.app.domain.building.document.BuildingKeywordCounts;
import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.AgencyReviews;
import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.dto.ReviewDetailResponse;
import JJinBBang.app.domain.building.entity.DormReviews;
import JJinBBang.app.domain.building.entity.DormitoryFacilities;
import JJinBBang.app.domain.building.entity.Facilities;
import JJinBBang.app.domain.building.entity.GeneralReviews;
import JJinBBang.app.domain.building.entity.ReviewDetails;
import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.domain.building.enums.BuildingType;
import JJinBBang.app.domain.building.enums.ReviewType;
import JJinBBang.app.domain.building.enums.UsageType;
import JJinBBang.app.domain.common.dto.PaginatedResponse;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.common.enums.KeywordType;
import lombok.RequiredArgsConstructor;

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

	@Override
	@Transactional(readOnly = true)
	public PaginatedResponse<ReviewSummaryResponse> getReviewList(Long buildingId, Boolean isAgency, Users user, PageRequest pageRequest) {

		Page<Reviews> reviewPage;
		if(isAgency) {
			Agencies agency = agenciesRepository.findById(buildingId).orElseThrow(BuildingNullException::new);
			reviewPage = reviewsRepository.findAllByAgency(agency, pageRequest);
		} else {
			Buildings building = buildingsRepository.findById(buildingId).orElseThrow(BuildingNullException::new);
			reviewPage = reviewsRepository.findAllByBuilding(building, pageRequest);
		}

		return PaginatedResponse.of(
				reviewPage,
				(review -> {
					Boolean liked = review.getReviewLikes().stream().anyMatch(reviewLike -> reviewLike.getUser().equals(user));
					ReviewDetails reviewDetail = reviewDetailsRepository.findByReviewId(review.getId()).orElseThrow(() -> new ReviewDetailInternalException(review.getId()));
					return ReviewSummaryResponse.of(review, liked, reviewDetail);
				})
		);
	}

    @Override
    @Transactional(readOnly = true)
    public ReviewDetailResponse getReviewDetail(Long reviewId, Users user) {
        Reviews review = reviewsRepository.findById(reviewId).orElseThrow(ReviewNotFoundException::new);

		Boolean liked = review.getReviewLikes().stream()
			.anyMatch(like -> like.getUser().equals(user));

		ReviewDetails reviewDetail = reviewDetailsRepository.findByReviewId(reviewId).orElseThrow(
			() -> new MissingReviewDetailException(reviewId));

		if (review instanceof GeneralReviews generalReview) {
			return ReviewDetailResponse.ofGeneral(generalReview, reviewDetail, liked);
		} else if (review instanceof DormReviews dormReview) {
			return ReviewDetailResponse.ofDormitory(dormReview, reviewDetail, liked);
		} else if (review instanceof AgencyReviews agencyReview) {
			return ReviewDetailResponse.ofAgency(agencyReview, reviewDetail, liked);
		} else {
			throw ReviewInternalServerErrorException.notSupportReviewType();
		}
	}

	@Override
	@Transactional
	public CreateReviewResponse createReview(ReviewRequest reviewRequest, Users user, ReviewType reviewType) {
		if (reviewType == ReviewType.GENERAL) {
			return CreateReviewResponse.from(createGeneralReview(reviewRequest, user));
		} else if (reviewType == ReviewType.DORM) {
			return CreateReviewResponse.from(createDormitoryReview(reviewRequest, user));
		} else if (reviewType == ReviewType.AGENCY) {
			return CreateReviewResponse.from(createAgencyReview(reviewRequest, user));
		} else {
			throw ReviewInternalServerErrorException.notSupportReviewType();
		}
	}

	public Long createGeneralReview(ReviewRequest dto, Users user) {
		// 1. Main review 생성
		Buildings building = findOrCreateBuilding(dto.buildingRequest());

		GeneralReviews generalReviewEntity = dto.toGeneralReviews(user, building);
		GeneralReviews savedReview = reviewsRepository.save(generalReviewEntity);

		// 3. review_details 저장
		ReviewDetails details = ReviewDetails.builder()
			.reviewId(savedReview.getId())
			.buildingId(building.getId())
			.buildingType(dto.buildingRequest().type())
			.images(dto.imageUrls())
			.imageCount(dto.imageUrls().size())
			.keywords(dto.keywords())
			.build();
		reviewDetailsRepository.save(details);

		// 4. 키워드 집계
		updateKeywordCounts(building.getId(), false, dto.keywords().positive());

		// 5. 건물 통계 업데이트
		building.incrementReviewCount();
		building.updateAverageRating(savedReview.getRating());
		if (!dto.imageUrls().isEmpty()) {
			building.incrementImagesCount();
		}
		buildingsRepository.save(building);

		return savedReview.getId();
	}

	public Long createDormitoryReview(ReviewRequest dto, Users user) {
		Buildings building = findOrCreateBuilding(dto.buildingRequest());


		DormReviews dormitoryReview = dto.toDormReviews(user, building, dto.condition());
		DormReviews savedReview = reviewsRepository.save(dormitoryReview);
		List<DormitoryFacilities> dormitoryFacilityList = convertToDormitoryFacilityList(dto.facilities(), savedReview);
		dormitoryFacilitiesRepository.saveAll(dormitoryFacilityList);


		// 4. review_details 저장
		ReviewDetails details = ReviewDetails.builder()
			.reviewId(savedReview.getId())
			.buildingId(building.getId())
			.buildingType(dto.buildingRequest().type())
			.images(dto.imageUrls())
			.imageCount(dto.imageUrls().size())
			.keywords(dto.keywords())
			.build();
		reviewDetailsRepository.save(details);

		// 5. 키워드 집계
		updateKeywordCounts(building.getId(), false, dto.keywords().positive());

		// 6. 건물 통계 업데이트
		building.incrementReviewCount();
		building.updateAverageRating(savedReview.getRating());
		if (!dto.imageUrls().isEmpty()) {
			building.incrementImagesCount();
		}
		buildingsRepository.save(building);

		return savedReview.getId();
	}

	public Long createAgencyReview(ReviewRequest dto, Users user) {
		// 1. Main review 생성
		Agencies agency = findOrCreateAgency(dto.buildingRequest());
		AgencyReviews agencyReview = dto.toAgencyReviews(user, agency);
		AgencyReviews savedReview = reviewsRepository.save(agencyReview);

		// 3. review_details 저장
		ReviewDetails details = ReviewDetails.builder()
			.reviewId(savedReview.getId())
			.buildingId(agency.getAgencyId())
			.buildingType(dto.buildingRequest().type())
			.images(dto.imageUrls())
			.imageCount(dto.imageUrls().size())
			.keywords(dto.keywords())
			.build();
		reviewDetailsRepository.save(details);

		// 4. 키워드 집계
		updateKeywordCounts(agency.getAgencyId(), true, dto.keywords().positive());

		// 5. 건물 통계 업데이트
		agency.incrementReviewCount();
		agency.updateAverageRating(savedReview.getRating());
		if (!dto.imageUrls().isEmpty()) {
			agency.incrementImagesCount();
		}
		agenciesRepository.save(agency);

		return savedReview.getId();
	}

	private Buildings findOrCreateBuilding(BuildingRequest dto) {
		return buildingsRepository.findByBuildingCode(dto.buildingCode())
			// 기존에 있는 건물인 경우,
			.map(existing -> {
				// 1) 건물의 건물 유형 가져와서
				List<BuildingType> buildingtypeList =  new ArrayList<>(existing.getBuildingType());

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
				Buildings saved = buildingsRepository.save(dto.toBuildingEntity());
				BuildingKeywordCounts kwCount = BuildingKeywordCounts.of(saved.getId(), false);
				buildingKeywordCountsRepository.save(kwCount);
				return saved;
			});
	}

	private Agencies findOrCreateAgency(BuildingRequest dto) {
		return agenciesRepository.findByBuildingCode(dto.buildingCode())
			.orElseGet(() -> {
				Agencies savedAgency = agenciesRepository.save(dto.toAgencyEntity());
				BuildingKeywordCounts buildingKeywordCount = BuildingKeywordCounts.of(
					savedAgency.getAgencyId(), true);
				buildingKeywordCountsRepository.save(buildingKeywordCount);
				return savedAgency;
			});
	}

	public void updateKeywordCounts(Long buildingId, Boolean isAgency, List<KeywordType> positives) {
		BuildingKeywordCounts buildingKeywordCounts = buildingKeywordCountsRepository.findByBuildingIdAndIsAgency(buildingId, isAgency)
			.orElseGet(() -> BuildingKeywordCounts.of(buildingId, isAgency));

		buildingKeywordCounts.incrementPositiveKeywords(positives);
		buildingKeywordCountsRepository.save(buildingKeywordCounts);
	}

	private List<DormitoryFacilities> convertToDormitoryFacilityList(FacilitiesDto dto, DormReviews dormReview) {
		List<DormitoryFacilities> entities = new ArrayList<>();

		// public 시설들
		for (String name : dto.publicFacilities()) {
			Facilities facility = facilitiesRepository.findByName(name).orElseThrow(BuildingNotFoundException::unsupportedDormitoryFacility);
			entities.add(DormitoryFacilities.create(dormReview, facility, true, UsageType.PUBLIC));
		}

		// private 시설들
		for (String name : dto.privateFacilities()) {
			Facilities facility = facilitiesRepository.findByName(name).orElseThrow(BuildingNotFoundException::unsupportedDormitoryFacility);
			entities.add(DormitoryFacilities.create(dormReview, facility, true, UsageType.PRIVATE));
		}

		// 휴게시설 추가
		Facilities lounge = facilitiesRepository.findByName("lounge").orElseThrow(BuildingNotFoundException::unsupportedDormitoryFacility);
		entities.add(DormitoryFacilities.create(dormReview, lounge, dto.lounge(), null));

		return entities;
	}
}
