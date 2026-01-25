package JJinBBang.app.domain.building.service;

import JJinBBang.app.domain.building.dto.BuildingDetailResponse;
import JJinBBang.app.domain.building.dto.DormitoryListResponse;
import JJinBBang.app.domain.user.entity.Users;

public interface BuildingService {
	BuildingDetailResponse getBuildingDetail(Long buildingId, Users user);

	DormitoryListResponse getDormitoryList(Long campusId);
}
