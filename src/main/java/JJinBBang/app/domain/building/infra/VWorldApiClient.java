package JJinBBang.app.domain.building.infra;

import JJinBBang.app.domain.building.dto.VWorldWfsGetFeatureRequest;
import JJinBBang.app.domain.building.dto.VWorldFeatureCollection;
import JJinBBang.app.domain.building.dto.VWorld2DDataResponse;

public interface VWorldApiClient {
	VWorld2DDataResponse searchBuildingByPoint(Double longitude, Double latitude);

	VWorldFeatureCollection searchAgencies(VWorldWfsGetFeatureRequest vWorldRequest);
}
