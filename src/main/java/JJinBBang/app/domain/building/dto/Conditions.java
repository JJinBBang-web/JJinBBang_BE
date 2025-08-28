package JJinBBang.app.domain.building.dto;

import JJinBBang.app.domain.building.entity.DormReviews;
import lombok.Builder;

@Builder
public record Conditions(
	String currentRegion,
	Double currentGrade
) {
	public static Conditions of(DormReviews dormReviews) {
		return Conditions.builder()
			.currentRegion(dormReviews.getCurrentRegion())
			.currentGrade(dormReviews.getCurrentGrade())
			.build();
	}
}
