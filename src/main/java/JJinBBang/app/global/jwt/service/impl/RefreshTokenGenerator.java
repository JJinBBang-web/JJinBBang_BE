package JJinBBang.app.global.jwt.service.impl;

import static JJinBBang.app.global.jwt.enums.TokenType.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.jwt.service.JwtTokenGenerator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class RefreshTokenGenerator implements JwtTokenGenerator {

	private final SecretKey signingKey;
	private final String tokenIssuer;
	private final String tokenAudience;
	/** Refresh Token 유효기간 (밀리초) */
	private final long refreshTokenTtlMillis;

	public RefreshTokenGenerator(
			@Value("${jwt.secret}") String base64Secret,
			@Value("${jwt.issuer}") String issuer,
			@Value("${jwt.audience}") String audience,
			@Value("${jwt.expiration-time.refresh-token}") long refreshTokenTtlMillis) {
		this.signingKey = Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(base64Secret));
		this.tokenIssuer = issuer;
		this.tokenAudience = audience;
		this.refreshTokenTtlMillis = refreshTokenTtlMillis;
	}

	/** 세션 ID를 반드시 포함해서 발급 */
	public String generate(Users user, String sessionId) {
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(refreshTokenTtlMillis, ChronoUnit.MILLIS);

		return Jwts.builder()
				.id(UUID.randomUUID().toString()) // jti
				.header().add("typ", "JWT").and()
				.issuer(tokenIssuer)
				.audience().add(tokenAudience).and()
				.subject(String.valueOf(user.getUserId()))
				.issuedAt(Date.from(issuedAt))
				.expiration(Date.from(expiresAt))
				.claim("type", REFRESH)
				.claim("sessionId", sessionId) // 세션 식별자
				.signWith(signingKey, Jwts.SIG.HS256)
				.compact();
	}

	/** 기존 인터페이스 호환(세션 없는 호출 방지용) — 사용 지양 */
	@Override
	public String generate(Users user) {
		// 세션을 반드시 쓰도록 강제
		return generate(user, UUID.randomUUID().toString());
	}
}
