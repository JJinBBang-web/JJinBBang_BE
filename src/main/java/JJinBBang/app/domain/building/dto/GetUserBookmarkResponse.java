package JJinBBang.app.domain.building.dto;

import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.entity.GeneralReviews;
import JJinBBang.app.domain.building.entity.Reviews;
import lombok.Builder;

@Builder
public record GetUserBookmarkResponse(
        GeneralReviewInfo generalReviewInfo,
        ReviewInfo reviewInfo,
        String image
) {
    public static GetUserBookmarkResponse of(Reviews review, GeneralReviews generalReview, Buildings building){
        return GetUserBookmarkResponse.builder()
                .generalReviewInfo(GeneralReviewInfo.of(review, generalReview, building))
                .reviewInfo(ReviewInfo.of(review))
                .image("http://localhost:8080/image/1.jpg")
                .build();
    }
}
