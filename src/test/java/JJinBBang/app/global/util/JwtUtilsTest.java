package JJinBBang.app.global.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.service.UsersService;
import JJinBBang.app.global.common.enums.Provider;
import JJinBBang.app.global.jwt.JwtUtils;
import JJinBBang.app.global.jwt.exception.InvalidTokenException;
import io.jsonwebtoken.ExpiredJwtException;

@SpringBootTest
public class JwtUtilsTest {
	@Value("${jwt.secret}")
	private String secretKey;

	private JwtUtils jwtUtils;
	private Users testUser;
	@Autowired
	private UsersService usersService;

	@BeforeEach
	public void setUp() {
		long accessTokenExpiration = 1000 * 60 * 60; // 1시간
		long refreshTokenExpiration = 1000 * 60 * 60 * 24 * 7; // 7일

		jwtUtils = new JwtUtils(secretKey, accessTokenExpiration, refreshTokenExpiration);
		testUser = Users.builder()
			.provider(Provider.kakao)
			.providerId("123541" + Provider.kakao.name())
			.build();
		try {
			testUser = usersService.save(testUser);
		} catch (Exception e) {
			System.out.println("e.getMessage() = " + e.getMessage());
			System.out.println("User already exists");
		}
	}

	@Test
	public void generateUserTest() {
		System.out.println(testUser.getProvider());
		System.out.println(testUser.getProviderId());
		System.out.println(testUser.getAdmissionCertificate());
		System.out.println(testUser.getCreatedAt());
		System.out.println(testUser.getVerificationStatus().name());
	}

	@Test
	void generateAccessTokenTest() {
		String token = jwtUtils.generateAccessToken(testUser);
		assertNotNull(token);
		assertFalse(token.isEmpty());
		System.out.println("token = " + token);
	}

	@Test
	void generateRefreshTokenTest() {
		String token = jwtUtils.generateRefreshToken(testUser);
		assertNotNull(token);
		assertFalse(token.isEmpty());
		System.out.println("token = " + token);
	}

	@Test
	void getProviderIdFromTokenTest() {
		String token = jwtUtils.generateAccessToken(testUser);
		String providerId = jwtUtils.getProviderIdFromToken(token);
		assertEquals(testUser.getProviderId(), providerId);
		System.out.println("providerId = " + providerId);
	}

	@Test
	void validateValidTokenTest() {
		String token = jwtUtils.generateAccessToken(testUser);
		assertDoesNotThrow(() -> jwtUtils.validateToken(token));
	}

	@Test
	void validateInvalidTokenTest() {
		String invalidToken = "invalid.token.string";
		assertThrows(RuntimeException.class, () -> jwtUtils.validateToken(invalidToken));
	}

	@Test
	void getAuthenticationTest() {
		String token = jwtUtils.generateAccessToken(testUser);
		assertEquals(testUser.getVerificationStatus(), jwtUtils.getVerificationStatus(token));
	}

	@Test
	void validateExpiredTokenTest() throws InterruptedException {
		JwtUtils shortLivedJwtUtils = new JwtUtils(
			secretKey,
			1000,
			1000);
		String token = shortLivedJwtUtils.generateAccessToken(testUser);
		Thread.sleep(1500); // 토큰 만료 대기
		assertThrows(InvalidTokenException.class, () -> shortLivedJwtUtils.validateToken(token));
	}
}
