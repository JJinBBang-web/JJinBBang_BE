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
	Keywords keywords,

	// 기숙사 전용
	@Valid
	Conditions condition,
	@Valid
	FacilitiesDto facilities
){
	// 정확히 하나의 리뷰 섹션만 전송됐는지 검증
	@AssertTrue(message = "generalReview, dormitoryReview, agencyReview 중 하나만 전송해야 합니다.")
	private boolean isExactlyOneReview() {
		int cnt = 0;
		if (generalReview    != null) cnt++;
		if (dormitoryReview  != null) cnt++;
		if (agencyReview     != null) cnt++;
		return cnt == 1;
	}

	@AssertTrue(message = "기숙사 리뷰인 경우 condition 및 facilities 필드는 필수입니다.")
	private boolean isDormitoryDetailsProvided() {
		if (dormitoryReview != null) {
			return condition   != null
				&& facilities  != null;
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

	public GeneralReviews toGeneralReviews(Users user, Buildings building) {
		return GeneralReviews.builder()
			.dtype(ReviewType.GENERAL)
			.likesCount(0)
			.thumbnailImage(imageUrls.isEmpty() ? null : imageUrls.getFirst())
			.content(generalReview.getContent())
			.tags(keywords.positive().stream().limit(3).toList())
			.rating(generalReview.getRating())
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

	public DormReviews toDormReviews(Users user, Buildings building, Conditions condition) {
		return DormReviews.builder()
			.dtype(ReviewType.DORM)
			.likesCount(0)
			.thumbnailImage(imageUrls.isEmpty() ? null : imageUrls.getFirst())
			.content(dormitoryReview.getContent())
			.rating(dormitoryReview.getRating())
			.user(user)
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
			.rating(agencyReview.getRating())
			.user(user)
			.agency(agency)
			.build();
	}


	public ReviewDetails toReviewDetails(Long reviewId, Long buildingId) {
		return ReviewDetails.builder()
				.reviewId(reviewId)
				.buildingId(buildingId)
				.buildingType(buildingRequest.type())
				.images(imageUrls)
				.imageCount(imageUrls.size())
				.keywords(keywords)
				.build();
	}
}
