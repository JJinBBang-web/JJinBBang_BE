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

	/**
	 * 이메일 인증 완료 시 유저의 학교 이메일과 인증 상태를 업데이트함
	 * @param user 유저
	 * @param universityEmail 인증에 사용한 학교 이메일
	 * @return 업데이트된 유저
	 */
	Users verifyUniversityEmail(Users user, String universityEmail);
}
