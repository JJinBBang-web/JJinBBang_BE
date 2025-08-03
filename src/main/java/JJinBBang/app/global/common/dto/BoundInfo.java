package JJinBBang.app.global.common.dto;

import lombok.Builder;

@Builder
public record BoundInfo(
	Double latitude,
	Double longitude
) {
	public static BoundInfo of(Double latitude, Double longitude) {
		return BoundInfo.builder()
			.latitude(latitude)
			.longitude(longitude)
			.build();
	}
}