package JJinBBang.app.global.common.dto;

import JJinBBang.app.domain.building.entity.Agencies;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class AgencyBuildingInfo {
    private Long id;
    private Boolean liked;
    private List<String> type;
    private String name;
    private String address;
    private BigDecimal rating;
    private Integer reviewCount;

    public static AgencyBuildingInfo of(Agencies agency, Boolean liked) {
        return AgencyBuildingInfo.builder()
                .id(agency.getId())
                .liked(liked)
                .type(List.of("AGENCY"))
                .name(agency.getName())
                .address(agency.getAddress())
                .rating(agency.getRating())
                .reviewCount(agency.getReviewCount())
                .build();
    }
}