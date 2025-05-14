package JJinBBang.app.global.common.dto;

import JJinBBang.app.domain.building.entity.Buildings;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class DormitoryBuildingInfo {
    private Long id;
    private String campus;
    private Boolean liked;
    private List<String> type;
    private String name;
    private String address;
    private BigDecimal rating;
    private Integer reviewCount;

    public static DormitoryBuildingInfo of(Buildings building, Boolean liked) {
        return DormitoryBuildingInfo.builder()
                .id(building.getId())
                .liked(liked)
                .type(List.of("DORMITORY"))
                .campus(building.getCampus().getCampusName())
                .name(building.getBuildingName())
                .address(building.getBuildingAddress())
                .rating(building.getBuildingRating())
                .reviewCount(building.getReviewCount())
                .build();
    }
}