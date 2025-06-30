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

import static org.junit.jupiter.api.Assertions.*;

/**
 * 디버깅용: 임의의 provider, providerId로 유저를 저장하고 Access Token을 발급
 */
@SpringBootTest
public class AccessTokenGeneratorTest {
    @Autowired
    private UsersService usersService;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    @DisplayName("1) 새 유저를 저장하고 그 유저에 대한 Access Token 발급")
    void givenNewUser_whenSaveAndIssueToken_thenTokenValid() {
        // --- 1) 새 Users 엔티티 생성 (DB에 없음) ---
        Users newUser = Users.builder()
                .provider(Provider.kakao)
                .providerId("debug-user")
                .build();

        // --- 2) DB에 저장 ---
        Users saved = usersService.save(newUser);
        assertNotNull(saved.getUserId(), "저장 후 userId 가 설정되어야 한다");

        // --- 3) Access Token 발급 ---
        String token = jwtUtils.generateAccessToken(saved);
        assertNotNull(token);
        assertFalse(token.isBlank(), "발급된 토큰은 빈 문자열이 아니어야 한다");

        // --- 4) 토큰 파싱 및 claim 검증 ---
        Claims claims = jwtUtils.parseClaims(token);
        assertEquals(saved.getProviderId(), claims.getSubject());
        assertEquals(
                VerificationStatus.UNVERIFIED.name(),
                claims.get("verificationStatus", String.class)
        );
        assertEquals(
                TokenType.ACCESS.getType(),
                claims.get("tokenType", String.class)
        );
        System.out.println("access token = " + token);
    }

    @Test
    @DisplayName("2) 이미 저장된 유저 ID 로 Users 조회 후 Access Token 발급")
    void givenExistingUserId_whenLoadAndIssueToken_thenTokenValid() {
        String existingId = "debug-user";
        assertNotNull(existingId);

        // --- 2) UsersService 로 유저 조회 ---
        Users loaded = usersService.findByProviderId(existingId);

        // --- 3) Access Token 발급 ---
        String token = jwtUtils.generateAccessToken(loaded);
        assertNotNull(token);
        assertFalse(token.isBlank());

        // --- 4) 파싱해서 subject 와 custom claim 확인 ---
        Claims claims = jwtUtils.parseClaims(token);
        assertEquals(loaded.getProviderId(), claims.getSubject());
        assertEquals(
                VerificationStatus.UNVERIFIED.name(),
                claims.get("verificationStatus", String.class)
        );
        assertEquals(TokenType.ACCESS.getType(), claims.get("tokenType", String.class));
        System.out.println("access token = " + token);
    }
}
