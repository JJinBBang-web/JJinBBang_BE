package JJinBBang.app.global.common.dto;

import lombok.Builder;

@Builder
public record MarkerInfo (
	Long id,
	Double latitude,
	Double longitude
){
	public static MarkerInfo from(Long id, Double latitude, Double longitude) {
		return MarkerInfo.builder()
			.id(id)
			.latitude(latitude)
			.longitude(longitude)
			.build();
	}
}
