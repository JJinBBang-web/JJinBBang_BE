package JJinBBang.app.global.common.dto;

import JJinBBang.app.domain.building.entity.GeneralReviews;
import JJinBBang.app.domain.building.enums.BuildingType;
import JJinBBang.app.domain.building.enums.ContractType;
import java.math.BigDecimal;

import JJinBBang.app.domain.building.enums.Floor;
import lombok.Builder;

@Builder
public record GeneralReviewInfo(
        Long id,
        String name,
        BuildingType type,
        ContractType contractType,
        Integer deposit,
        Integer price,
        Floor floor,
        Double space,
        Integer maintenanceCost,
        BigDecimal rating,
        Boolean liked
) {

    public static GeneralReviewInfo of(GeneralReviews generalReview, Boolean liked) {
        return GeneralReviewInfo.builder()
            .id(generalReview.getId())
            .name(generalReview.getBuilding().getBuildingName())
            .type(generalReview.getBuildingType())
            .contractType(generalReview.getContractType())
            .deposit(generalReview.getDeposit())
            .price(generalReview.getPrice())
            .floor(generalReview.getFloor())
            .space(generalReview.getArea())
            .maintenanceCost(generalReview.getMaintenanceCost())
            .rating(generalReview.getRating())
            .liked(liked)
            .build();
    }
}
