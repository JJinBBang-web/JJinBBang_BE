package JJinBBang.app.global.jwt;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.common.enums.VerificationStatus;
import JJinBBang.app.global.error.exception.AuthGroupException;
import JJinBBang.app.global.jwt.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

@Slf4j
@Component
public class JwtUtils {

	private final SecretKey key;
	private final long accessTokenExpiration;
	private final long refreshTokenExpiration;

	private final long signupTokenExpiration;

	public JwtUtils(
		@Value("${jwt.secret}") String secretKey,
		@Value("${jwt.expiration-time.access-token}") long accessTokenExpiration,
		@Value("${jwt.expiration-time.refresh-token}") long refreshTokenExpiration,
		@Value("${jwt.expiration-time.signup-token}") long signupTokenExpiration
	) {
		this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
		this.accessTokenExpiration = accessTokenExpiration;
		this.refreshTokenExpiration = refreshTokenExpiration;
		this.signupTokenExpiration = signupTokenExpiration;
	}

	public String generateAccessToken(Users user) {
		return generateToken(user, accessTokenExpiration);
	}

	public String generateRefreshToken(Users user) {
		return generateToken(user, refreshTokenExpiration);
	}

	public String generateSignupToken(Users user) {
		return signupToken(user, signupTokenExpiration);
	}

	private String generateToken(Users user, long expirationTime) {
		return Jwts.builder()
			.subject(user.getProviderId())
			.claim("verificationStatus", user.getVerificationStatus().name())
			.claim("tokenType", "auth")
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + expirationTime))
			.signWith(key)
			.compact();
	}

	private String signupToken(Users user, long expirationTime) {
		return Jwts.builder()
			.subject(user.getProviderId())
			.claim("provider", user.getProvider().name())
			.claim("tokenType", "signup")
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + expirationTime))
			.signWith(key)
			.compact();
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
