package JJinBBang.app.global.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.entity.DormReviews;
import JJinBBang.app.domain.building.entity.GeneralReviews;
import JJinBBang.app.domain.building.entity.Reviews;
import lombok.Builder;

@Builder
public record SearchInfoDto(
	@JsonInclude(JsonInclude.Include.NON_NULL)
	GeneralReviewInfo generalReviewInfo,

	@JsonInclude(JsonInclude.Include.NON_NULL)
	GeneralBuildingInfo generalBuildingInfo,

	@JsonInclude(JsonInclude.Include.NON_NULL)
	DormitoryReviewInfo dormitoryReviewInfo,

	@JsonInclude(JsonInclude.Include.NON_NULL)
	DormitoryBuildingInfo dormitoryBuildingInfo,

	@JsonInclude(JsonInclude.Include.NON_NULL)
	AgencyReviewInfo agencyReviewInfo,

	@JsonInclude(JsonInclude.Include.NON_NULL)
	AgencyBuildingInfo agencyBuildingInfo,

	ReviewInfo reviewInfo,
	BoundInfo boundInfo,
	String image
) {
	public static SearchInfoDto ofSearchGeneralReviewInfo(GeneralReviews generalReview, Boolean liked) {
		return SearchInfoDto.builder()
			.generalReviewInfo(GeneralReviewInfo.of(generalReview, liked))
			.reviewInfo(ReviewInfo.of(generalReview))
			.boundInfo(BoundInfo.of(generalReview.getBuilding().getBuildingLat(), generalReview.getBuilding().getBuildingLot()))
			.image(generalReview.getThumbnailImage())
			.build();
	}

	public static SearchInfoDto ofSearchGeneralBuildingInfo(Reviews review, Buildings building, Boolean liked) {
		return SearchInfoDto.builder()
			.generalBuildingInfo(GeneralBuildingInfo.of(building, liked))
			.reviewInfo(ReviewInfo.ofBuilding(review, building))
			.boundInfo(BoundInfo.of(building.getBuildingLat(), building.getBuildingLot()))
            .image(review != null ? review.getThumbnailImage() : null)
			.build();
	}

	public static SearchInfoDto ofSearchDormitoryReviewInfo(DormReviews dormReview, Buildings building, String universityName, Boolean liked) {
		return SearchInfoDto.builder()
			.dormitoryReviewInfo(DormitoryReviewInfo.of(dormReview, building, universityName, liked))
			.reviewInfo(ReviewInfo.of(dormReview))
			.boundInfo(BoundInfo.of(building.getBuildingLat(), building.getBuildingLot()))
			.image(dormReview.getThumbnailImage())
			.build();
	}

	public static SearchInfoDto ofSearchDormitoryBuildingInfo(Reviews review, Buildings building, String universityName, Boolean liked) {
		return SearchInfoDto.builder()
			.dormitoryBuildingInfo(DormitoryBuildingInfo.of(building, universityName, liked))
			.reviewInfo(ReviewInfo.ofBuilding(review, building))
			.boundInfo(BoundInfo.of(building.getBuildingLat(), building.getBuildingLot()))
            .image(review != null ? review.getThumbnailImage() : null)
			.build();
	}

    public static SearchInfoDto ofSearchAgencyReviewInfo(Reviews review, Agencies agency, Boolean liked) {
        if (review == null) {
            return ofSearchAgencyBuildingInfo(null, agency, liked);
        }
        return SearchInfoDto.builder()
                .agencyReviewInfo(AgencyReviewInfo.of(review, agency, liked))
                .reviewInfo(ReviewInfo.of(review))
                .boundInfo(BoundInfo.of(agency.getAgencyLat(), agency.getAgencyLot()))
                .image(review.getThumbnailImage())
                .build();
    }

	public static SearchInfoDto ofSearchAgencyBuildingInfo(Reviews review, Agencies agency, Boolean liked) {
		return SearchInfoDto.builder()
			.agencyBuildingInfo(AgencyBuildingInfo.of(agency, liked))
			.reviewInfo(ReviewInfo.ofAgency(review, agency))
			.boundInfo(BoundInfo.of(agency.getAgencyLat(), agency.getAgencyLot()))
			.image("http://localhost:8080/image/1.jpg")
			.build();
	}
}