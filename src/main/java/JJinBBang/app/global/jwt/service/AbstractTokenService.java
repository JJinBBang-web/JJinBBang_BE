package JJinBBang.app.global.jwt.service;

import JJinBBang.app.domain.user.entity.Users;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.util.Date;

public abstract class AbstractTokenService implements TokenService {

    protected final SecretKey secretKey;
    protected final long expirationTime;
    private final String tokenType;

    public AbstractTokenService(SecretKey secretKey, long expirationTime, String tokenType) {
        this.secretKey = secretKey;
        this.expirationTime = expirationTime;
        this.tokenType = tokenType;
    }

    @Override
    public String generateToken(Users user) {
        return Jwts.builder()
                .subject(user.getProviderId())
                .claim("verificationStatus", user.getVerificationStatus().name())
                .claim("provider", user.getProvider().name())
                .claim("tokenType", tokenType)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }

}
