package JJinBBang.app.global.jwt;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.common.enums.VerificationStatus;
import JJinBBang.app.global.jwt.exception.InvalidTokenException;
import JJinBBang.app.global.jwt.service.RefreshTokenService;
import JJinBBang.app.global.jwt.service.TokenGenerateService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

import javax.crypto.SecretKey;

@Slf4j
@Component
public class JwtUtils {

	private final SecretKey key;

	private final TokenGenerateService accessTokenGenerator;
	private final TokenGenerateService refreshTokenGenerator;
	private final TokenGenerateService signupTokenGenerator;
	private final RefreshTokenService refreshTokenService;


	public JwtUtils(
            @Value("${jwt.secret}") String secretKey,
			@Qualifier("accessTokenService") TokenGenerateService accessTokenGenerator,
			@Qualifier("refreshTokenService") TokenGenerateService refreshTokenGenerator,
			@Qualifier("signupTokenService") TokenGenerateService signupTokenGenerator,
			@Qualifier("refreshTokenService") RefreshTokenService refreshTokenService
    ) {
		this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
		this.accessTokenGenerator = accessTokenGenerator;
		this.refreshTokenGenerator = refreshTokenGenerator;
		this.signupTokenGenerator = signupTokenGenerator;
		this.refreshTokenService = refreshTokenService;
    }

	public String generateAccessToken(Users user) {
		return accessTokenGenerator.generateToken(user);
	}

	public String generateRefreshToken(Users user) {
		return refreshTokenGenerator.generateToken(user);
	}

	public String generateSignupToken(Users user) {
		return signupTokenGenerator.generateToken(user);
	}

	public String reissueAccessToken(Users user, String refreshToken) {
		// 리프레시 토큰 검증
		if (!validateToken(refreshToken)) {
			log.error("Invalid refresh token.");
			throw InvalidTokenException.invalidToken();
		}

		if(!refreshTokenService.validateRefreshToken(user.getUserId(), refreshToken)){
			throw InvalidTokenException.invalidToken();
		}

		// 새로운 액세스 토큰 생성
		return generateAccessToken(user);
	}


	public String extractToken(String authorizationHeader) {
		return authorizationHeader.substring(7);
	}

	public String extractToken(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
	}


	public String getProviderIdFromToken(String token) {
		Claims claims = parseClaims(token);
		return claims.getSubject();
	}

	public VerificationStatus getVerificationStatus(String token) {
		String status = parseClaims(token).get("verificationStatus", String.class);
		return VerificationStatus.valueOf(status);
	}


	public Map<String, Object> extractAllClaimsAsMap(String token) {
		return parseClaims(token);
	}

	public Claims parseClaims(String token) {
		return Jwts.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (ExpiredJwtException e) {
			log.error("JWT expired.");
			throw InvalidTokenException.expired();
		} catch (JwtException | IllegalArgumentException e) {
			// 토큰 만료 이외의 JWT 오류는 보안상 유효하지 않은 토큰 예외로 통일
			log.error("Invalid JWT format.");
			throw InvalidTokenException.invalidToken();
		}
		return true;
	}
}
