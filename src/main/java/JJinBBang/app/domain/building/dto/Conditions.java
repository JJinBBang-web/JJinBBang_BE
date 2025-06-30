package JJinBBang.app.domain.building.dto;

import JJinBBang.app.domain.building.entity.DormReviews;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record Conditions(
	@NotNull
	String currentRegion,
	@NotNull
	Double currentGrade
) {
	public static Conditions of(DormReviews dormReviews) {
		return Conditions.builder()
			.currentRegion(dormReviews.getCurrentRegion())
			.currentGrade(dormReviews.getCurrentGrade())
			.build();
	}
}
