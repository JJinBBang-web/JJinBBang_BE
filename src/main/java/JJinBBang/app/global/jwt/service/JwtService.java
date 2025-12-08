package JJinBBang.app.global.jwt.service;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.jwt.dto.TokenPair;
import io.jsonwebtoken.Claims;

public interface JwtService {

	/** 최초 로그인, 로그인 당 세션 1개 발급 */
	TokenPair generateTokenPair(Users user);

	/** 리프레시, 엑세스 로테이션 재발급 */
	TokenPair rotate(Users user, String refreshToken);

	/** 로그아웃, 현재 세션 리프레시 토큰 블랙리스트 */
	void logout(Long userId, String refreshToken);

	/** 전체 로그아웃, 모든 세션 리프레시 토큰 블랙리스트 */
	void logoutAll(Long userId);

	/** Bearer 추출 */
	String extractBearerTokenFromHeader(String authorizationHeader);

	/** 파싱 */
	Claims parseClaims(String token);
}
