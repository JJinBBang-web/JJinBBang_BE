package JJinBBang.app.domain.building.service;

import JJinBBang.app.domain.building.dto.*;
import JJinBBang.app.domain.building.enums.ReviewType;
import JJinBBang.app.domain.common.dto.PaginatedResponse;
import JJinBBang.app.domain.user.entity.Users;

public interface ReviewService {
	ReviewDetailResponse getReviewDetail(Long reviewId, Users user);
	PaginatedResponse<ReviewSummaryResponse> getReviewList(Long buildingId, Boolean isAgency, Users user, PageRequest pageRequest);
	CreateReviewResponse createReview(ReviewRequest reviewRequest, Users user, ReviewType reviewType);
}
