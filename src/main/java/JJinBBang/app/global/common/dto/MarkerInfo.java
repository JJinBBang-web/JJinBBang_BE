package JJinBBang.app.global.common.dto;

import lombok.Builder;

@Builder
public record MarkerInfo (
	Long id,
	Double latitude,
	Double longitude,
	Boolean isReviews
){
	public static MarkerInfo from(Long id, Double latitude, Double longitude, Boolean isReviews) {
		return MarkerInfo.builder()
			.id(id)
			.latitude(latitude)
			.longitude(longitude)
			.isReviews(isReviews)
			.build();
	}
}
