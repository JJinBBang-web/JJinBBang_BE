package JJinBBang.app.global.common.dto;

import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.AgencyReviews;
import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.domain.building.enums.BuildingType;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AgencyReviewInfo(
        Long id,
        String name,
        BuildingType type,
        BigDecimal rating,
        Boolean liked
) {
    public static AgencyReviewInfo of(Reviews reviews, Agencies agencies, Boolean liked) {
        return AgencyReviewInfo.builder()
                .id(reviews != null ? reviews.getId() : null)
                .name(agencies.getName())
                .type(BuildingType.AGENCY)
                .rating(reviews != null ? reviews.getRating() : null)
                .liked(liked)
                .build();

    }

    public static AgencyReviewInfo of(AgencyReviews agencyReview, Boolean liked) {
        return AgencyReviewInfo.builder()
            .id(agencyReview.getId())
            .name(agencyReview.getAgency().getName())
            .type(BuildingType.AGENCY)
            .rating(agencyReview.getRating())
            .liked(liked)
            .build();

    }
}
