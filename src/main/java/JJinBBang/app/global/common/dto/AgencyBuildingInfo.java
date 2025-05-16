package JJinBBang.app.global.common.dto;

import JJinBBang.app.domain.building.entity.Agencies;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record AgencyBuildingInfo(
        Long id,
        String name,
        List<String> type,
        String address,
        BigDecimal rating,
        Boolean liked,
        Integer reviewCount
) {
    public static AgencyBuildingInfo of(Agencies agencies, Boolean liked) {
        return AgencyBuildingInfo.builder()
                .id(agencies.getAgencyId())
                .name(agencies.getName())
                .type(List.of("공인중개사"))
                .address(agencies.getAddress())
                .rating(agencies.getRating())
                .liked(liked)
                .reviewCount(agencies.getReviewCount())
                .build();

    }
}
