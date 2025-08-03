package JJinBBang.app.domain.building.repository.custom;

import java.util.List;
import JJinBBang.app.domain.building.entity.Agencies;

public interface AgenciesRepositoryCustom {
	List<Agencies> searchAgencies(String keyword);

	List<Agencies> findMarkersWithinBounds(
		Double neLat, Double neLng,
		Double swLat, Double swLng
	);
}
