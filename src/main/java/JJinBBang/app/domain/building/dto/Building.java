package JJinBBang.app.domain.building.dto;

import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.enums.BuildingType;
import lombok.Builder;

@Builder
public record Building (
	Long buildingId,
	String buildingCode,
	String name,
	BuildingType type,
	String address,
	Double latitude,
	Double longitude
){
	public static Building of(Buildings building, BuildingType buildingType) {
		return Building.builder()
			.buildingId(building.getId())
			.buildingCode(building.getBuildingCode())
			.name(building.getBuildingName())
			.type(buildingType)
			.address(building.getBuildingAddress())
			.latitude(building.getBuildingLat())
			.longitude(building.getBuildingLat())
			.build();
	}

	public static Building of(Agencies agency, BuildingType buildingType) {
		return Building.builder()
			.buildingId(agency.getAgencyId())
			.buildingCode(agency.getAgencySerial())
			.name(agency.getName())
			.type(buildingType)
			.address(agency.getAddress())
			.latitude(agency.getAgencyLat())
			.longitude(agency.getAgencyLat())
			.build();
	}
}
