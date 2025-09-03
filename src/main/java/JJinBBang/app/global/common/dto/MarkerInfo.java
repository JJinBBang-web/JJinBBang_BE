package JJinBBang.app.global.common.dto;

import JJinBBang.app.domain.building.enums.BuildingType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
public record MarkerInfo (
	Long id,
	BuildingType type,
	Double latitude,
	Double longitude,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Boolean hasReview
){
	public static MarkerInfo fromReview(Long id, BuildingType type, Double latitude, Double longitude) {
		return MarkerInfo.builder()
			.id(id)
			.type(type)
			.latitude(latitude)
			.longitude(longitude)
			.build();
	}

    public static MarkerInfo fromBuidling(Long id, BuildingType type, Double latitude, Double longitude, Boolean hasReview) {
        return MarkerInfo.builder()
            .id(id)
            .type(type)
            .latitude(latitude)
            .longitude(longitude)
            .hasReview(hasReview)
            .build();
    }
}
