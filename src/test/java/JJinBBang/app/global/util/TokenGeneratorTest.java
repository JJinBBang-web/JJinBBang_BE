package JJinBBang.app.global.util;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.service.UsersService;
import JJinBBang.app.global.common.enums.Provider;
import JJinBBang.app.global.common.enums.VerificationStatus;
import JJinBBang.app.global.jwt.JwtUtils;
import JJinBBang.app.global.jwt.enums.TokenType;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("prod")
class TokenGeneratorTest {

    @Autowired
    private UsersService usersService;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    @DisplayName("디버그 유저 생성 후 Access/Refresh Token 발급 및 검증")
    void generateDebugUserTokens() {
        // --- 1) 디버그 유저 생성 및 저장 ---
        String providerId = "debug-user";
        Users saved;
        if(usersService.existsByProviderId(providerId)) {
            saved = usersService.findByProviderId(providerId);
        } else {
            Users user = Users.builder()
                .provider(Provider.kakao)
                .providerId(providerId)
                .build();
            saved = usersService.save(user);
        }

        assertNotNull(saved.getUserId(), "저장된 유저는 userId 가 있어야 한다");

        // --- 2) Access Token 발급 ---
        String accessToken = jwtUtils.generateAccessToken(saved);
        assertNotNull(accessToken);
        assertFalse(accessToken.isBlank());

        Claims accessClaims = jwtUtils.parseClaims(accessToken);
        assertEquals(saved.getProviderId(), accessClaims.getSubject());
        assertEquals(
            VerificationStatus.UNVERIFIED.name(),
            accessClaims.get("verificationStatus", String.class)
        );
        assertEquals(TokenType.ACCESS.getType(), accessClaims.get("tokenType", String.class));

        System.out.println("access token = " + accessToken);

        // --- 3) Refresh Token 발급 ---
        String refreshToken = jwtUtils.generateRefreshToken(saved);
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isBlank());

        Claims refreshClaims = jwtUtils.parseClaims(refreshToken);
        assertEquals(saved.getProviderId(), refreshClaims.getSubject());
        assertEquals(
            VerificationStatus.UNVERIFIED.name(),
            refreshClaims.get("verificationStatus", String.class)
        );
        assertEquals(TokenType.REFRESH.getType(), refreshClaims.get("tokenType", String.class));

        System.out.println("refresh token = " + refreshToken);
    }
}
