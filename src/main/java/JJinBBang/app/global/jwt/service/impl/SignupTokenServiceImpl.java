package JJinBBang.app.global.jwt.service.impl;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.jwt.service.AbstractTokenService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service("signupTokenService")
public class SignupTokenServiceImpl extends AbstractTokenService {

    public SignupTokenServiceImpl(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration-time.signup-token}") long signupTokenExpiration
    ) {
        super(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)), signupTokenExpiration, "signup");
    }


    @Override
    public String generateToken(Users user) {
        return Jwts.builder()
                .subject(user.getProviderId())
                .claim("provider", user.getProvider().name())
                .claim("tokenType", "signup")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + super.expirationTime))
                .signWith(super.secretKey)
                .compact();
    }
}
