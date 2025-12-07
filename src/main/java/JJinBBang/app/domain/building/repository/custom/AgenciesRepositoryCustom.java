package JJinBBang.app.domain.building.repository.custom;

import JJinBBang.app.global.common.enums.KeywordType;
import java.util.List;
import JJinBBang.app.domain.building.entity.Agencies;

public interface AgenciesRepositoryCustom {
	List<Agencies> searchAgencies(String keyword);

	List<Agencies> findMarkersWithinBounds(
			Double neLat, Double neLng,
			Double swLat, Double swLng,
			List<KeywordType> reviewKeywords);
}
