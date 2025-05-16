package JJinBBang.app.domain.building.dto;

import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.entity.GeneralReviews;
import JJinBBang.app.domain.building.entity.ReviewLikes;
import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.domain.building.enums.ContractType;
import JJinBBang.app.domain.building.enums.Floor;
import JJinBBang.app.domain.building.enums.ReviewType;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record GeneralReviewInfo(
        Long id,
        String name,
        String type,
        String contractType,
        Integer deposit,
        Integer monthlyRent,
        String floor,
        Double space,
        Integer maintenanceCost,
        BigDecimal rating,
        Boolean liked
) {
    public static GeneralReviewInfo of(GeneralReviews generalReview, Buildings building, Boolean liked) {
        return GeneralReviewInfo.builder()
                .id(generalReview.getId())
                .name(building.getBuildingName())
                .type(building.getBuildingType())
                .contractType(generalReview.getContractType().getDescription())
                .deposit(generalReview.getDeposit())
                .monthlyRent(generalReview.getPrice())
                .floor(generalReview.getFloor().getDescription())
                .space(generalReview.getArea())
                .maintenanceCost(generalReview.getMaintenanceCost())
                .rating(generalReview.getRating())
                .liked(liked)
                .build();
    }
}
