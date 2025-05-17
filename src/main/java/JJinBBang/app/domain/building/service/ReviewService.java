package JJinBBang.app.domain.building.service;

import JJinBBang.app.domain.building.dto.ReviewDetailResponse;
import JJinBBang.app.domain.user.entity.Users;

public interface ReviewService {
	ReviewDetailResponse getReviewDetail(Long reviewId, Users user);
}
