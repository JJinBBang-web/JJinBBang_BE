package JJinBBang.app.global.util;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.common.enums.VerificationStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
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

	public JwtUtils(
		@Value("${jwt.secret}") String secretKey,
		@Value("${jwt.expiration-time.access-token}") long accessTokenExpiration,
		@Value("${jwt.expiration-time.refresh-token}") long refreshTokenExpiration
	) {
		this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
		this.accessTokenExpiration = accessTokenExpiration;
		this.refreshTokenExpiration = refreshTokenExpiration;
	}

	public String generateAccessToken(Users user) {
		return generateToken(user, accessTokenExpiration);
	}

	public String generateRefreshToken(Users user) {
		return generateToken(user, refreshTokenExpiration);
	}

	private String generateToken(Users user, long expirationTime) {
		return Jwts.builder()
			.subject(user.getProviderId())
			.claim("verificationStatus", user.getVerificationStatus().name())
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + expirationTime))
			.signWith(key)
			.compact();
	}



	public String getTokenFromHeader(String authorizationHeader) {
		return authorizationHeader.substring(7);
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

	private Claims parseClaims(String token) {
		return Jwts.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	public void validateToken(String token) {
		try {
			Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (ExpiredJwtException e) {
			log.error("JWT expired.");
			// throw InvalidTokenException.expired();
			throw e;
		} catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException e) {
			log.error("Invalid JWT format.");
			// throw InvalidTokenException.invalidToken();
			throw e;
		}
	}
}
