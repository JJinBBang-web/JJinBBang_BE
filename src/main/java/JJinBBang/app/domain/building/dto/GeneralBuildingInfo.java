package JJinBBang.app.domain.building.dto;

import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.BuildingLikes;
import JJinBBang.app.domain.building.entity.Buildings;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record GeneralBuildingInfo(
        Long id,
        String name,
        String type,
        String address,
        BigDecimal rating,
        Boolean liked,
        Integer reviewCount
) {
    public static GeneralBuildingInfo of(Buildings building, Boolean liked) {
        return GeneralBuildingInfo.builder()
                .id(building.getId())
                .name(building.getBuildingName())
                .type(building.getBuildingType())
                .address(building.getBuildingAddress())
                .rating(building.getBuildingRating())
                .liked(liked)
                .reviewCount(building.getReviewCount())
                .build();

    }
}
