package JJinBBang.app.domain.building.dto;

import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.Buildings;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AgencyBuildingInfo(
        Long id,
        String name,
        String type,
        String address,
        BigDecimal rating,
        Boolean liked,
        Integer reviewCount
) {
    public static AgencyBuildingInfo of(Agencies agencies, Boolean liked) {
        return AgencyBuildingInfo.builder()
                .id(agencies.getAgencyId())
                .name(agencies.getName())
                .type("공인중개사")
                .address(agencies.getAddress())
                .rating(agencies.getRating())
                .liked(liked)
                .reviewCount(agencies.getReviewCount())
                .build();

    }
}
