package JJinBBang.app.domain.building.dto;

import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.domain.building.enums.Floor;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record DormitoryBuildingInfo(
        Long id,
        String name,
        String type,
        String universityName,
        String address,
        BigDecimal rating,
        Boolean liked
) {
    public static DormitoryBuildingInfo of(Buildings building, String universityName, Boolean liked) {
        return DormitoryBuildingInfo.builder()
                .id(building.getId())
                .name(building.getBuildingName())
                .type(building.getBuildingType())
                .universityName(universityName)
                .address(building.getBuildingAddress())
                .rating(building.getBuildingRating())
                .liked(liked)
                .build();
    }
}
