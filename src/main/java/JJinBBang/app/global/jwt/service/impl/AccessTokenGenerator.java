package JJinBBang.app.global.jwt.service.impl;

import static JJinBBang.app.global.jwt.enums.TokenType.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.jwt.service.JwtTokenGenerator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class AccessTokenGenerator implements JwtTokenGenerator {

	private final SecretKey signingKey;
	private final String tokenIssuer;
	private final String tokenAudience;
	/** Access Token 유효기간 (밀리초) */
	private final long accessTokenTtlMillis;

	public AccessTokenGenerator(
			@Value("${jwt.secret}") String base64Secret,
			@Value("${jwt.issuer}") String issuer,
			@Value("${jwt.audience}") String audience,
			@Value("${jwt.expiration-time.access-token:3600000}") long accessTokenTtlMillis) {
		this.signingKey = Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(base64Secret));
		this.tokenIssuer = issuer;
		this.tokenAudience = audience;
		this.accessTokenTtlMillis = accessTokenTtlMillis;
	}

	@Override
	public String generate(Users user) {
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(accessTokenTtlMillis, ChronoUnit.MILLIS);

		return Jwts.builder()
				.header().add("typ", "JWT").and()
				.issuer(tokenIssuer)
				.audience().add(tokenAudience).and()
				.subject(String.valueOf(user.getUserId()))
				.issuedAt(Date.from(issuedAt))
				.expiration(Date.from(expiresAt))
				.claim("type", ACCESS)
				.claim("role", user.getRole().toString())
				.signWith(signingKey, Jwts.SIG.HS256)
				.compact();
	}
}
