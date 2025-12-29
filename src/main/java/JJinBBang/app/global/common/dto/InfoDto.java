package JJinBBang.app.global.common.dto;

import JJinBBang.app.domain.building.entity.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
public record InfoDto(
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
        String image,
        Integer imageCount) {
    public static InfoDto ofGeneralReviewInfo(GeneralReviews generalReview, Boolean liked, Integer imageCount) {
        return InfoDto.builder()
                .generalReviewInfo(GeneralReviewInfo.of(generalReview, liked))
                .reviewInfo(ReviewInfo.of(generalReview))
                .image(generalReview.getThumbnailImage())
                .imageCount(imageCount)
                .build();
    }

    public static InfoDto ofGeneralBuildingInfo(Reviews review, Buildings building, Boolean liked, Integer imageCount) {
        return InfoDto.builder()
                .generalBuildingInfo(GeneralBuildingInfo.of(building,liked))
                .reviewInfo(ReviewInfo.ofBuilding(review,building))
                .image(review != null ? review.getThumbnailImage() : null)
                .imageCount(imageCount)
                .build();
    }

    public static InfoDto ofDormitoryReviewInfo(DormReviews dormReview, Buildings building, String universityName,
            Boolean liked, Integer imageCount) {
        return InfoDto.builder()
                .dormitoryReviewInfo(DormitoryReviewInfo.of(dormReview,building,universityName,liked))
                .reviewInfo(ReviewInfo.of(dormReview))
                .image(dormReview.getThumbnailImage())
                .imageCount(imageCount)
                .build();
    }

    public static InfoDto ofDormitoryBuildingInfo(Reviews review, Buildings building, String universityName,
            Boolean liked, Integer imageCount) {
        return InfoDto.builder()
                .dormitoryBuildingInfo(DormitoryBuildingInfo.of(building,universityName,liked))
                .reviewInfo(ReviewInfo.ofBuilding(review,building))
                .image(review != null ? review.getThumbnailImage() : null)
                .imageCount(imageCount)
                .build();
    }

    public static InfoDto ofAgencyReviewInfo(Reviews review, Agencies agency, Boolean liked, Integer imageCount) {
        return InfoDto.builder()
                .agencyReviewInfo(AgencyReviewInfo.of(review,agency,liked))
                .reviewInfo(ReviewInfo.of(review))
                .image(review.getThumbnailImage())
                .imageCount(imageCount)
                .build();
    }

    public static InfoDto ofAgencyBuildingInfo(Reviews review, Agencies agency, Boolean liked, Integer imageCount) {
        return InfoDto.builder()
                .agencyBuildingInfo(AgencyBuildingInfo.of(agency,liked))
                .reviewInfo(ReviewInfo.ofAgency(review,agency))
                .image(review != null ? review.getThumbnailImage() : null)
                .imageCount(imageCount)
                .build();
    }
}
