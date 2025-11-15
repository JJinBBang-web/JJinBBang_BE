package JJinBBang.app.domain.building.infra;

import JJinBBang.app.domain.building.dto.VWorldAddressToCoordRequest;
import JJinBBang.app.domain.building.dto.VWorldAddressToCoordResponse;
import JJinBBang.app.domain.building.dto.VWorldEBOfficeRequest;
import JJinBBang.app.domain.building.dto.VWorldEBOfficeResponse;
import JJinBBang.app.domain.building.dto.VWorldWfsResponse;

public interface VWorldApiClient {
	VWorldWfsResponse searchBuildingByPoint(Double longitude, Double latitude);

	VWorldEBOfficeResponse searchAgencies(VWorldEBOfficeRequest vWorldRequest);

	VWorldAddressToCoordResponse geocode(VWorldAddressToCoordRequest request);
}
