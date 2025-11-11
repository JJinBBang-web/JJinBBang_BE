package JJinBBang.app.domain.building.repository.custom;

import java.util.List;

import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.enums.BuildingType;
import JJinBBang.app.domain.building.enums.ContractType;
import JJinBBang.app.global.common.enums.KeywordType;

public interface BuildingsRepositoryCustom {
	List<Buildings> findMarkersWithinBounds(
		Double neLat, Double neLng,
		Double swLat, Double swLng,
		List<BuildingType> buildTypes,
		ContractType contractType,
		Integer depositMin, Integer depositMax,
		Integer monthlyRentMin, Integer monthlyRentMax,
		Boolean inMaintenanceCost,
		List<KeywordType> reviewKeywords
	);

	List<Buildings> searchBuildings(
		String keyword,
		List<BuildingType> buildTypes,
		ContractType contractType,
		Integer depositMin, Integer depositMax,
		Integer monthlyRentMin, Integer monthlyRentMax,
		Boolean inMaintenanceCost,
		List<KeywordType> reviewKeywords
	);
}
