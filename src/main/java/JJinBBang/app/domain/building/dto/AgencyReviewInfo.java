package JJinBBang.app.domain.building.dto;

import JJinBBang.app.domain.building.entity.Agencies;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AgencyReviewInfo(
        Long id,
        String name,
        String type,
        BigDecimal rating,
        Boolean liked
) {
    public static AgencyReviewInfo of(Agencies agencies, Boolean liked) {
        return AgencyReviewInfo.builder()
                .id(agencies.getAgencyId())
                .name(agencies.getName())
                .type("공인중개사")
                .rating(agencies.getRating())
                .liked(liked)
                .build();

    }
}
