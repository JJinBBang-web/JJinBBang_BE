package JJinBBang.app.domain.building.entity;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewLikeId implements Serializable {
	private Long reviewId;
	private Long userId;

	public static ReviewLikeId of(Long reviewId, Long userId) {
		if (reviewId == null || userId == null) {
			throw new IllegalArgumentException("ReviewId UserId는 null이 될 수 없습니다.");
		}
		return new ReviewLikeId(reviewId, userId);
	}
}