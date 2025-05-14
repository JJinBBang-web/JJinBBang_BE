package JJinBBang.app.domain.building.dto;

import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.entity.GeneralReviews;
import JJinBBang.app.domain.building.entity.Reviews;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Builder
public record ReviewInfo(
        String content,
        List<String> keyword,
        Integer likeCount,
        LocalDateTime updateAt
) {
    public static ReviewInfo of(Reviews review) {
        return ReviewInfo.builder()
                .content(review.getContent())
                .keyword(Arrays.asList("PO_LO_01", "PO_MT_01", "PO_MT_04"))
                .likeCount(review.getLikesCount())
                .updateAt(review.getUpdatedAt())
                .build();
    }
}
