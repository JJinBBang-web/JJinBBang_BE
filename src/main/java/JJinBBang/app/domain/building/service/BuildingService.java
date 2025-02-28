package JJinBBang.app.domain.building.service;

import JJinBBang.app.domain.building.dto.BuildingDetailResponse;

public interface BuildingService {
	BuildingDetailResponse getBuildingDetail(Long buildingId, Long userId);
}
