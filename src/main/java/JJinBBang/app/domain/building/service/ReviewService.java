package JJinBBang.app.domain.building.service;

import JJinBBang.app.domain.building.dto.PageRequest;
import JJinBBang.app.domain.building.dto.ReviewSummaryResponse;
import JJinBBang.app.domain.common.dto.PaginatedResponse;
import JJinBBang.app.domain.user.entity.Users;

public interface ReviewService {
	PaginatedResponse<ReviewSummaryResponse> getReviewList(Long buildingId, Boolean isAgency, Users user, PageRequest pageRequest);
}
