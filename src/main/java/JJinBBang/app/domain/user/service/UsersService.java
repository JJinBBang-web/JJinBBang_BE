package JJinBBang.app.domain.user.service;

import JJinBBang.app.domain.building.dto.UserReviewListResponse;
import JJinBBang.app.domain.user.dto.UserInfoResponseDto;
import JJinBBang.app.domain.user.entity.Users;

public interface UsersService {

	boolean existsByProviderId(String providerId);

	Users findByProviderId(String providerId);

	Users save(Users user);

	UserInfoResponseDto getUserInfo(Users user);

	UserReviewListResponse getUserReviews(Users user, int offset, int limit, String orderby);

}
