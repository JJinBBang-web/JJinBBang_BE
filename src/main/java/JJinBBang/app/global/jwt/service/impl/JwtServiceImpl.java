package JJinBBang.app.global.jwt.service.impl;

import static JJinBBang.app.global.jwt.enums.TokenType.*;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.jwt.dto.TokenPair;
import JJinBBang.app.global.jwt.enums.RotationResult;
import JJinBBang.app.global.jwt.enums.TokenType;
import JJinBBang.app.global.jwt.exception.InvalidTokenException;
import JJinBBang.app.global.jwt.repository.RefreshTokenRepository;
import JJinBBang.app.global.jwt.service.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtVerifier jwtVerifier;
	private final RefreshTokenGenerator refreshTokenGenerator;
	private final AccessTokenGenerator accessTokenGenerator;

	@Value("${jwt.refresh-ttl-minutes:43200}")
	private long refreshTtlMinutes;

	/** 재사용 탐지 시 모든 세션 폐기 여부(운영 정책) */
	@Value("${jwt.revoke-all-on-reuse:false}")
	private boolean revokeAllOnReuse;

	private Duration refreshTtl() { return Duration.ofMinutes(refreshTtlMinutes); }

	// -----------------------
	// 발급/재발급/로그아웃
	// -----------------------

	@Override
	@Transactional
	public TokenPair generateTokenPair(Users user) {
		// 서버가 세션 ID 발급
		String sessionId = UUID.randomUUID().toString();

		// Refresh 발급(+ jti, sid)
		String refreshToken = refreshTokenGenerator.generate(user, sessionId);
		Claims refreshClaims = parseClaims(refreshToken);
		String refreshTokenId = refreshClaims.getId();

		// (userId, sessionId) 단위로 저장
		refreshTokenRepository.saveOrUpdate(user.getUserId(), sessionId, refreshTokenId, refreshTtl());

		// Access 발급
		String accessToken = accessTokenGenerator.generate(user);
		return new TokenPair(accessToken, refreshToken);
	}

	@Override
	@Transactional
	public TokenPair rotate(Users user, String refreshToken) {
		Claims claims = parseClaims(refreshToken);

		// 타입 확인
		String tokenType = claims.get("type", String.class);
		if (REFRESH != TokenType.valueOf(tokenType))
			throw InvalidTokenException.accessTokenNotAllowed();

		String presentedRefreshTokenId = claims.getId();
		Long subjectUserId = Long.valueOf(claims.getSubject());

		// subject 확인
		if (!user.getUserId().equals(subjectUserId)) {
			throw InvalidTokenException.invalidToken();
		}

		// 세션 ID 확인(sid)
		String sessionId = claims.get("sessionId", String.class);
		if (sessionId == null || sessionId.isEmpty()) {
			throw InvalidTokenException.invalidToken();
		}

		// 블랙리스트 확인
		if (refreshTokenRepository.isBlacklisted(presentedRefreshTokenId)) {
			throw InvalidTokenException.invalidToken();
		}

		Duration presentedTokenTtl = ttlRemaining(claims);

		// 새 토큰을 미리 생성(동시성 경쟁에서 실패하면 버려도 무해)
		String newRefreshToken = refreshTokenGenerator.generate(user, sessionId); // 같은 세션 유지
		Claims newRefreshClaims = jwtVerifier.requireAndParse(newRefreshToken);
		String newRefreshTokenId = newRefreshClaims.getId();

		// 로그인 세션 단위 원자 회전 시도
		RotationResult rotationResult = refreshTokenRepository.tryRotate(
			user.getUserId(),
			sessionId,
			presentedRefreshTokenId,
			newRefreshTokenId,
			presentedTokenTtl,
			refreshTtl(),
			revokeAllOnReuse
		);

		if (rotationResult != RotationResult.SUCCESS) {
			// 등록 안 됨
			// 재사용/불일치
			throw InvalidTokenException.invalidToken();
		}
		String newAccessToken = accessTokenGenerator.generate(user);
		return new TokenPair(newAccessToken, newRefreshToken);
	}

	@Override
	@Transactional
	public void logout(Long userId, String refreshToken) {
		Claims claims = parseClaims(refreshToken);
		String sessionId = claims.get("sessionId", String.class);// 세션 ID 확인
		// 현재 세션 로그아웃
		refreshTokenRepository.revokeSession(userId, sessionId, refreshTtl());
	}

	@Override
	@Transactional
	public void logoutAll(Long userId) {
		// 모든 세션 로그아웃
		refreshTokenRepository.revokeAllSessions(userId, refreshTtl());
	}

	// -----------------------
	// 헬퍼
	// -----------------------

	@Override
	public String extractBearerTokenFromHeader(String authorizationHeader) {
		if (authorizationHeader == null) return null;
		String trimmedHeader = authorizationHeader.trim();
		if (trimmedHeader.length() < 8) return null;
		if (!trimmedHeader.regionMatches(true, 0, "Bearer ", 0, 7)) return null;
		return trimmedHeader.substring(7).trim();
	}

	@Override
	public Claims parseClaims(String token) {
		return jwtVerifier.requireAndParse(token);
	}

	private Duration ttlRemaining(Claims claims) {
		long remainingSeconds =
			claims.getExpiration().toInstant().getEpochSecond() - Instant.now().getEpochSecond();
		return Duration.ofSeconds(Math.max(0, remainingSeconds));
	}
}
