package JJinBBang.app.global.common.dto;

import JJinBBang.app.domain.building.entity.Buildings;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public record GeneralBuildingInfo(
        Long id,
        String name,
        List<String> type,
        String address,
        BigDecimal rating,
        Boolean liked,
        Integer reviewCount
) {
    public static GeneralBuildingInfo of(Buildings building, Boolean liked) {
        return GeneralBuildingInfo.builder()
                .id(building.getId())
                .name(building.getBuildingName())
                .type(
                        Arrays.stream(building.getBuildingType().split(",\\s*"))
                                .collect(Collectors.toList())
                )                .address(building.getBuildingAddress())
                .rating(building.getBuildingRating())
                .liked(liked)
                .reviewCount(building.getReviewCount())
                .build();

    }
}
