package JJinBBang.app.domain.building.dto;

import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.entity.GeneralReviews;
import JJinBBang.app.domain.building.entity.ReviewLikes;
import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.domain.building.enums.ContractType;
import JJinBBang.app.domain.building.enums.Floor;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record GeneralReviewInfo(
        Long id,
        String name,
        String type,
        ContractType contractType,
        Integer deposit,
        Integer monthlyRent,
        Floor floor,
        Double space,
        Integer maintenanceCost,
        BigDecimal rating,
        Boolean liked
) {
    public static GeneralReviewInfo of(Reviews review, GeneralReviews generalReview, Buildings building) {
        return GeneralReviewInfo.builder()
                .id(review.getId())
                .name(building.getBuildingName())
                .type("s")
                .contractType(generalReview.getContractType())
                .deposit(generalReview.getDeposit())
                .monthlyRent(generalReview.getPrice())
                .floor(generalReview.getFloor())
                .space(generalReview.getArea())
                .maintenanceCost(generalReview.getMaintenanceCost())
                .rating(review.getRating())
                .liked(true)
                .build();
    }
}
