package JJinBBang.app.global.common.dto;

import JJinBBang.app.domain.building.enums.BuildingType;
import lombok.Builder;

@Builder
public record MarkerInfo (
	Long id,
	BuildingType type,
	Double latitude,
	Double longitude
){
	public static MarkerInfo from(Long id, BuildingType type, Double latitude, Double longitude) {
		return MarkerInfo.builder()
			.id(id)
			.type(type)
			.latitude(latitude)
			.longitude(longitude)
			.build();
	}
}
