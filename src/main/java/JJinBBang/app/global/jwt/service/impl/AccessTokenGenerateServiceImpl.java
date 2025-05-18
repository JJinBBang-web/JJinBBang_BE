package JJinBBang.app.global.jwt.service.impl;

import JJinBBang.app.global.jwt.service.AbstractTokenGenerateService;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Slf4j
@Service("accessTokenService")
public class AccessTokenGenerateServiceImpl extends AbstractTokenGenerateService {
    public AccessTokenGenerateServiceImpl(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration-time.access-token}") long accessTokenExpiration
    ) {
        super(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)), accessTokenExpiration, "auth");
    }
}
