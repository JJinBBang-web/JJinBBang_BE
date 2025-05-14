package JJinBBang.app.domain.building.service;

import JJinBBang.app.domain.building.dto.BuildingDetailResponse;
import JJinBBang.app.domain.user.entity.Users;

public interface AgencyService {
    BuildingDetailResponse getAgencyDetail(Long buildingId, Users user);
}
