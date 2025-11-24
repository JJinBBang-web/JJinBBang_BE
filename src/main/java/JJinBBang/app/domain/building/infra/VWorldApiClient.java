package JJinBBang.app.domain.building.infra;

import JJinBBang.app.domain.building.dto.VWorldResponse;

public interface VWorldApiClient {
	VWorldResponse searchByPoint(Double longitude, Double latitude);
}
