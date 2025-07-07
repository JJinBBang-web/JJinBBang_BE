package JJinBBang.app.domain.building.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import JJinBBang.app.domain.building.entity.AgencyReviews;
import JJinBBang.app.domain.building.entity.DormReviews;
import JJinBBang.app.domain.building.entity.GeneralReviews;
import JJinBBang.app.domain.building.entity.ReviewDetails;
import JJinBBang.app.domain.building.enums.BuildingType;
import JJinBBang.app.global.common.dto.AgencyReviewInfo;
import JJinBBang.app.global.common.dto.DormitoryReviewInfo;
import JJinBBang.app.global.common.dto.GeneralReviewInfo;
import JJinBBang.app.global.common.dto.Keywords;
import JJinBBang.app.global.common.dto.ReviewInfo;
import lombok.Builder;

@Builder
public record ReviewDetailResponse(
	@JsonInclude(JsonInclude.Include.NON_NULL)
	GeneralReviewInfo generalReviewInfo,

	@JsonInclude(JsonInclude.Include.NON_NULL)
	DormitoryReviewInfo dormitoryReviewInfo,

	@JsonInclude(JsonInclude.Include.NON_NULL)
	AgencyReviewInfo agencyReviewInfo,

	ReviewInfo reviewInfo,

	ReviewImages reviewImages,

	Building building,

	Keywords keywords,

	@JsonInclude(JsonInclude.Include.NON_NULL)
	Conditions conditions,

	@JsonInclude(JsonInclude.Include.NON_NULL)
	FacilitiesDto facilities,

	Long authorId
) {
	public static ReviewDetailResponse ofGeneral(GeneralReviews generalReviews, ReviewDetails reviewDetail, Boolean liked) {
		return ReviewDetailResponse.builder()
			.generalReviewInfo(GeneralReviewInfo.of(generalReviews, reviewDetail, liked))
			.reviewInfo(ReviewInfo.of(generalReviews))
			.reviewImages(ReviewImages.from(generalReviews.getBuilding()))
			.building(Building.of(generalReviews.getBuilding(), reviewDetail.getBuildingType()))
			.keywords(reviewDetail.getKeywords())
			.authorId(generalReviews.getUser().getUserId())
			.build();
	}

	public static ReviewDetailResponse ofDormitory(DormReviews dormReview, ReviewDetails reviewDetail, Boolean liked) {
		return ReviewDetailResponse.builder()
			.dormitoryReviewInfo(DormitoryReviewInfo.of(dormReview, liked))
			.reviewInfo(ReviewInfo.of(dormReview))
			.reviewImages(ReviewImages.from(dormReview.getBuilding()))
			.building(Building.of(dormReview.getBuilding(), BuildingType.DORMITORY))
			.keywords(reviewDetail.getKeywords())
			.conditions(Conditions.of(dormReview))
			.facilities(FacilitiesDto.of(dormReview.getDormitoryFacilities()))
			.authorId(dormReview.getUser().getUserId())
			.build();
	}

	public static ReviewDetailResponse ofAgency(AgencyReviews agencyReview, ReviewDetails reviewDetail, Boolean liked) {
		return ReviewDetailResponse.builder()
			.agencyReviewInfo(AgencyReviewInfo.of(agencyReview, liked))
			.reviewInfo(ReviewInfo.of(agencyReview))
			.reviewImages(ReviewImages.from(agencyReview.getAgency()))
			.building(Building.of(agencyReview.getAgency(), BuildingType.AGENCY))
			.keywords(reviewDetail.getKeywords())
			.authorId(agencyReview.getUser().getUserId())
			.build();
	}
}
