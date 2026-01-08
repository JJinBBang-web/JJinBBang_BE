package JJinBBang.app.global.jwt.service.impl;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import JJinBBang.app.global.jwt.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtVerifier {

	private final SecretKey signingKey;
	private final String requiredIssuer;
	private final String requiredAudience;
	private final long allowedClockSkewSeconds;

	public JwtVerifier(
		@Value("${jwt.secret}") String base64Secret,
		@Value("${jwt.issuer}") String issuer,
		@Value("${jwt.audience}") String audience,
		@Value("${jwt.clock-skew-seconds:5}") long clockSkewSeconds
	) {
		this.signingKey = Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(base64Secret));
		this.requiredIssuer = issuer;
		this.requiredAudience = audience;
		this.allowedClockSkewSeconds = clockSkewSeconds;
	}

	public Claims requireAndParse(String token) {
		try {
			return Jwts.parser()
				.requireIssuer(requiredIssuer)
				.requireAudience(requiredAudience)
				.clockSkewSeconds(allowedClockSkewSeconds)
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (ExpiredJwtException exception) {
			throw InvalidTokenException.expired();
		} catch (Exception exception) {
			throw InvalidTokenException.invalidToken();
		}
	}
}
