package JJinBBang.app.global.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.common.enums.Provider;
import JJinBBang.app.global.common.enums.VerificationStatus;
import io.jsonwebtoken.ExpiredJwtException;

@SpringBootTest
public class JwtUtilsTest {

	private JwtUtils jwtUtils;
	private Users testUser;

	@BeforeEach
	public void setUp() {
		String secretKey = "b2qzzeohY7dsOBjKDxFDpVb4oXy0KNHVPEZoujByTBSHTpjyjX15jxqZ82ELXX95WTMhVZARzNxcC4ePd49hJg"; // Base64 encoded key
		long accessTokenExpiration = 1000 * 60 * 60; // 1시간
		long refreshTokenExpiration = 1000 * 60 * 60 * 24 * 7; // 7일

		jwtUtils = new JwtUtils(secretKey, accessTokenExpiration, refreshTokenExpiration);
		testUser = Users.builder()
			.provider(Provider.kakao)
			.providerId("123541" + Provider.kakao.name())
			.build();
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
			"b2qzzeohY7dsOBjKDxFDpVb4oXy0KNHVPEZoujByTBSHTpjyjX15jxqZ82ELXX95WTMhVZARzNxcC4ePd49hJg==",
			1000,
			1000);
		String token = shortLivedJwtUtils.generateAccessToken(testUser);
		Thread.sleep(1500); // 토큰 만료 대기
		assertThrows(ExpiredJwtException.class, () -> shortLivedJwtUtils.validateToken(token));
	}
}
