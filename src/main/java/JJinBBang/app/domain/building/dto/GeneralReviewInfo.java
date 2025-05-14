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
        ReviewType type,
        ContractType contractType,
        Integer deposit,
        Integer monthlyRent,
        Floor floor,
        Double space,
        Integer maintenanceCost,
        BigDecimal rating,
        Boolean liked
) {
    public static GeneralReviewInfo of(GeneralReviews generalReview, Buildings building, Boolean liked) {
        return GeneralReviewInfo.builder()
                .id(generalReview.getId())
                .name(building.getBuildingName())
                .type(generalReview.getDtype())
                .contractType(generalReview.getContractType())
                .deposit(generalReview.getDeposit())
                .monthlyRent(generalReview.getPrice())
                .floor(generalReview.getFloor())
                .space(generalReview.getArea())
                .maintenanceCost(generalReview.getMaintenanceCost())
                .rating(generalReview.getRating())
                .liked(liked)
                .build();
    }
}
