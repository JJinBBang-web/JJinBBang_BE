package JJinBBang.app.domain.building.dto;

import java.util.List;

import JJinBBang.app.domain.building.entity.*;
import JJinBBang.app.domain.building.enums.BuildingType;
import JJinBBang.app.domain.building.enums.ReviewType;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.common.dto.Keywords;
import jakarta.validation.Valid;

// 1. 최상위 Wrapper DTO
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest (

	// 세 가지 중 하나만 non-null 이어야 함
	@Valid
	GeneralReviewDto generalReview,

	@Valid
	DormitoryReviewDto dormitoryReview,

	@Valid
	AgencyReviewDto agencyReview,

	@NotNull
	List<@NotBlank String> imageUrls,

	@Valid
	BuildingRequest buildingRequest,

	@Valid
	@NotNull
	Keywords keywords,

	// 기숙사 전용
	Conditions condition,
	@Valid
	FacilitiesDto facilities
){
	@AssertTrue(message = "기숙사 리뷰인 경우 facilities 필드는 필수입니다.")
	private boolean isDormitoryDetailsProvided() {
		if (dormitoryReview != null) {
			return facilities  != null;
		}
		return true;
	}

	@AssertTrue(message = "리뷰 타입에 맞는 건물 유형을 선택해야 합니다")
	private boolean isProperBuildingType() {
		if (dormitoryReview != null) {
			return buildingRequest.type() == BuildingType.DORMITORY;
		}
		if (agencyReview != null) {
			return buildingRequest.type() == BuildingType.AGENCY;
		}
		if (generalReview != null) {
			return (buildingRequest.type() != BuildingType.AGENCY && buildingRequest.type() != BuildingType.DORMITORY);
		}
		return true;
	}

	@AssertTrue(message = "리뷰 유형에 따라 이미지 개수를 올바르게 설정해주세요. 일반/기숙사 리뷰는 2~20장, 중개사 리뷰는 최대 20장입니다.")
	private boolean isImageCountValid() {
		int size = imageUrls.size();
		if (agencyReview != null) {
			return size <= 20;
		}

		return size >= 1 && size <= 20;
	}


	// 정확히 하나의 리뷰 섹션만 전송됐는지 검증
	@AssertTrue(message = "generalReview, dormitoryReview, agencyReview 중 하나만 전송해야 합니다.")
	private boolean isExactlyOneReview() {
		int cnt = 0;
		if (generalReview    != null) cnt++;
		if (dormitoryReview  != null) cnt++;
		if (agencyReview     != null) cnt++;
		return cnt == 1;
	}


	// 긍정, 부정 및 건물 유형에 따른 키워드 유효성 검증
	@AssertTrue(message = "사용할 수 없는 키워드가 포함되어 있습니다.")
	private boolean isValidKeywords() {
		List<String> positiveKeywords = keywords.positive().stream()
				.map(Enum::toString)
				.toList();
		List<String> negativeKeywords = keywords.negative().stream()
				.map(Enum::toString)
				.toList();

		String typeCode;
		if (generalReview != null)   typeCode = "BD";
		else if (dormitoryReview != null) typeCode = "DO";
		else if (agencyReview != null)   typeCode = "AG";
		else return true;

		String posPrefix = "PO_" + typeCode + "_";
		String negPrefix = "NE_" + typeCode + "_";
		return positiveKeywords.stream().allMatch(k -> k.startsWith(posPrefix))
				&& negativeKeywords.stream().allMatch(k -> k.startsWith(negPrefix));
	}

	public GeneralReviews toGeneralReviews(Users user, Buildings building) {
		return GeneralReviews.builder()
			.dtype(ReviewType.GENERAL)
			.likesCount(0)
			.thumbnailImage(imageUrls.isEmpty() ? null : imageUrls.getFirst())
			.content(generalReview.getContent())
			.tags(keywords.positive().stream().limit(5).toList())
			.rating(generalReview.getRating())
			.buildingType(buildingRequest.type())
			.user(user)
			.building(building)
			.floor(generalReview.getFloor())
			.area(generalReview.getSpace())
			.contractType(generalReview.getContractType())
			.deposit(generalReview.getDeposit())
			.price(generalReview.getMonthlyRent())
			.maintenanceCost(generalReview.getMaintenanceCost())
			.build();
	}

	public GeneralReviews toUpdatedGeneralReviews(GeneralReviews oldReview, Buildings building) {
		return GeneralReviews.builder()
				.id(oldReview.getId())
				.dtype(oldReview.getDtype())
				.likesCount(oldReview.getLikesCount())
				.thumbnailImage(imageUrls.isEmpty() ? null : imageUrls.getFirst())
				.content(generalReview.getContent())
				.tags(keywords.positive().stream().limit(5).toList())
				.rating(generalReview.getRating())
				.buildingType(buildingRequest.type())
				.user(oldReview.getUser())
				.building(building)
				.floor(generalReview.getFloor())
				.area(generalReview.getSpace())
				.contractType(generalReview.getContractType())
				.deposit(generalReview.getDeposit())
				.price(generalReview.getMonthlyRent())
				.maintenanceCost(generalReview.getMaintenanceCost())
				.build();
	}

	public DormReviews toDormReviews(Users user, Buildings building) {
		return DormReviews.builder()
			.dtype(ReviewType.DORM)
			.likesCount(0)
			.thumbnailImage(imageUrls.isEmpty() ? null : imageUrls.getFirst())
			.content(dormitoryReview.getContent())
			.tags(keywords.positive().stream().limit(5).toList())
			.rating(dormitoryReview.getRating())
			.buildingType(BuildingType.DORMITORY)
			.user(user)
			.building(building)
			.floor(dormitoryReview.getFloor())
			.capacity(dormitoryReview.getCapacity())
			.dormFee(dormitoryReview.getDormFee())
			.currentRegion(condition.currentRegion())
			.currentGrade(condition.currentGrade())
			.build();
	}

	public DormReviews toUpdatedDormitoryReviews(DormReviews oldReview, Buildings building) {
		return DormReviews.builder()
				.id(oldReview.getId())
				.dtype(oldReview.getDtype())
				.likesCount(oldReview.getLikesCount())
				.thumbnailImage(imageUrls.isEmpty() ? null : imageUrls.getFirst())
				.content(dormitoryReview.getContent())
				.tags(keywords.positive().stream().limit(5).toList())
				.rating(dormitoryReview.getRating())
				.buildingType(BuildingType.DORMITORY)
				.user(oldReview.getUser())
				.building(building)
				.floor(dormitoryReview.getFloor())
				.capacity(dormitoryReview.getCapacity())
				.dormFee(dormitoryReview.getDormFee())
				.currentRegion(condition.currentRegion())
				.currentGrade(condition.currentGrade())
				.build();
	}

	public AgencyReviews toAgencyReviews(Users user, Agencies agency) {
		return AgencyReviews.builder()
			.dtype(ReviewType.AGENCY)
			.likesCount(0)
			.thumbnailImage(imageUrls.isEmpty() ? null : imageUrls.getFirst())
			.content(agencyReview.getContent())
			.tags(keywords.positive().stream().limit(5).toList())
			.rating(agencyReview.getRating())
			.buildingType(BuildingType.AGENCY)
			.user(user)
			.agency(agency)
			.build();
	}

	public AgencyReviews toUpdatedAgencyReviews(AgencyReviews oldReview, Agencies agency) {
		return AgencyReviews.builder()
				.id(oldReview.getId())
				.dtype(oldReview.getDtype())
				.likesCount(oldReview.getLikesCount())
				.thumbnailImage(imageUrls.isEmpty() ? null : imageUrls.getFirst())
				.content(agencyReview.getContent())
				.tags(keywords.positive().stream().limit(5).toList())
				.rating(agencyReview.getRating())
				.buildingType(BuildingType.AGENCY)
				.user(oldReview.getUser())
				.agency(agency)
				.build();
	}

	public ReviewDetails toReviewDetails(Long reviewId, Long buildingId) {
		return ReviewDetails.builder()
				.reviewId(reviewId)
				.buildingId(buildingId)
				.images(imageUrls)
				.imageCount(imageUrls.size())
				.keywords(keywords)
				.build();
	}

	public ReviewDetails toUpdatedReviewDetails(ReviewDetails oldDetails, Long buildingId) {
		return oldDetails.toBuilder()
				.buildingId(buildingId)
				.images(imageUrls)
				.imageCount(imageUrls.size())
				.keywords(keywords)
				.build();
	}
}
