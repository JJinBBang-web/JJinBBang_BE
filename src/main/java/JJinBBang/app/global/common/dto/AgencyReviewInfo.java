package JJinBBang.app.global.common.dto;

import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.Reviews;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record AgencyReviewInfo(
        Long id,
        String name,
        List<String> type,
        BigDecimal rating,
        Boolean liked
) {
    public static AgencyReviewInfo of(Reviews reviews,Agencies agencies, Boolean liked) {
        return AgencyReviewInfo.builder()
                .id(reviews.getId())
                .name(agencies.getName())
                .type(List.of("공인중개사"))
                .rating(agencies.getRating())
                .liked(liked)
                .build();

    }
}
