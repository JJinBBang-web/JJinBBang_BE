package JJinBBang.app.global.common.dto;

import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.enums.BuildingType;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record DormitoryBuildingInfo(
        Long id,
        String name,
        List<BuildingType> type,
        String universityName,
        String address,
        BigDecimal rating,
        Boolean liked,
        Integer reviewCount
) {
    public static DormitoryBuildingInfo of(Buildings building, String universityName, Boolean liked) {
        return DormitoryBuildingInfo.builder()
                .id(building.getId())
                .liked(liked)
                .type(List.of(BuildingType.DORMITORY))
                .name(building.getBuildingName())
                .universityName(universityName)
                .address(building.getBuildingAddress())
                .rating(building.getBuildingRating())
                .reviewCount(building.getReviewCount())
                .liked(liked)
                .build();
    }
}
