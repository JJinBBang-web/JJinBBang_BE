package JJinBBang.app.global.common.dto;

import JJinBBang.app.domain.building.entity.Buildings;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record DormitoryBuildingInfo(
        Long id,
        String name,
        List<String> type,
        String universityName,
        String address,
        BigDecimal rating,
        Boolean liked
) {
    public static DormitoryBuildingInfo of(Buildings building, String universityName, Boolean liked) {
        return DormitoryBuildingInfo.builder()
                .id(building.getId())
                .name(building.getBuildingName())
                .type(List.of("기숙사"))
                .universityName(universityName)
                .address(building.getBuildingAddress())
                .rating(building.getBuildingRating())
                .liked(liked)
                .build();
    }
}
