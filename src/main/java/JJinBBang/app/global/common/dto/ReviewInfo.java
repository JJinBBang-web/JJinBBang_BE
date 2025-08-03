package JJinBBang.app.global.common.dto;

import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.entity.Reviews;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import JJinBBang.app.global.common.enums.KeywordType;

@Builder
public record ReviewInfo(
        String content,
        List<KeywordType> keyword,
        Integer likeCount,
        LocalDateTime updateAt
) {
	public static ReviewInfo of(Reviews review) {
		return ReviewInfo.builder()
			.content(review.getContent())
			.keyword(review.getTags())
			.likeCount(review.getLikesCount())
			.updateAt(review.getUpdatedAt())
			.build();
	}
    public static ReviewInfo ofBuilding(Reviews review, Buildings building) {
        return ReviewInfo.builder()
				.content(review != null ? review.getContent() : null)
				.keyword(review != null ? review.getTags() : null)
                .likeCount(building.getLikeCount())
				.updateAt(review != null ? review.getUpdatedAt() : null)
                .build();
    }

    public static ReviewInfo ofAgency(Reviews review, Agencies agency) {
        return ReviewInfo.builder()
			.content(review != null ? review.getContent() : null)
			.keyword(review != null ? review.getTags() : null)
            .likeCount(agency.getLikeCount())
			.updateAt(review != null ? review.getUpdatedAt() : null)
            .build();
    }
}
