package JJinBBang.app.global.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.common.enums.Provider;
import JJinBBang.app.global.jwt.JwtUtils;
import JJinBBang.app.global.jwt.exception.InvalidTokenException;
import JJinBBang.app.global.jwt.repository.InMemoryRefreshTokenRepository;
import JJinBBang.app.global.jwt.service.RefreshTokenService;
import JJinBBang.app.global.jwt.service.TokenGenerateService;
import JJinBBang.app.global.jwt.service.impl.AccessTokenGenerateServiceImpl;
import JJinBBang.app.global.jwt.service.impl.RefreshTokenGenerateServiceImpl;
import JJinBBang.app.global.jwt.service.impl.SignupTokenGenerateServiceImpl;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

	// Base64로 인코딩된 32바이트(256비트) 비밀키
	private static final String SECRET_KEY =
			"MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";

	@Mock
	TokenGenerateService accessTokenGenerateService;
	@Mock
	TokenGenerateService refreshTokenGenerateService;
	@Mock
	TokenGenerateService signupTokenGenerateService;
	@Mock
	RefreshTokenService refreshTokenService;

	JwtUtils jwtUtils;  // Mockito가 생성자 인젝션 해 줌

	private Users testUser;

	@BeforeEach
	void setUp() {
		// 생성자에 SECRET_KEY와 mock TokenService들을 주입
		jwtUtils = new JwtUtils(
				SECRET_KEY,
				accessTokenGenerateService,
				refreshTokenGenerateService,
				signupTokenGenerateService,
				refreshTokenService
		);

		// DB 호출 없이 builder만 사용
		testUser = Users.builder()
				.provider(Provider.kakao)
				.providerId("user123")
				.build();
	}

	// ---------- Mockito 위임 테스트 ----------

	@Test
	void generateAccessToken_위임() {
		when(accessTokenGenerateService.generateToken(testUser))
				.thenReturn("ACCESS_TOKEN");

		String token = jwtUtils.generateAccessToken(testUser);

		assertEquals("ACCESS_TOKEN", token);
		verify(accessTokenGenerateService).generateToken(testUser);
	}

	@Test
	void generateRefreshToken_위임() {
		when(refreshTokenGenerateService.generateToken(testUser))
				.thenReturn("REFRESH_TOKEN");

		String token = jwtUtils.generateRefreshToken(testUser);

		assertEquals("REFRESH_TOKEN", token);
		verify(refreshTokenGenerateService).generateToken(testUser);
	}

	@Test
	void generateSignupToken_위임() {
		when(signupTokenGenerateService.generateToken(testUser))
				.thenReturn("SIGNUP_TOKEN");

		String token = jwtUtils.generateSignupToken(testUser);

		assertEquals("SIGNUP_TOKEN", token);
		verify(signupTokenGenerateService).generateToken(testUser);
	}

	@Test
	void extractToken_헤더에서_정상추출() {
		String bearer = "Bearer MY.JWT.TOKEN";
		assertEquals("MY.JWT.TOKEN", jwtUtils.extractToken(bearer));

		var req = new MockHttpServletRequest();
		req.addHeader("Authorization", bearer);
		assertEquals("MY.JWT.TOKEN", jwtUtils.extractToken(req));
	}

	@Test
	void validateToken_비어있는_토큰은_예외() {
		assertThrows(InvalidTokenException.class,
				() -> jwtUtils.validateToken(""));
	}

	// ---------- 실제 JWT 생성 → 파싱/검증 테스트 ----------

	@Test
	void realToken_parseClaimsAndGetProviderAndStatus() {
		// 1) 실제 TokenService 구현체 생성
		var accessSvc  = new AccessTokenGenerateServiceImpl(SECRET_KEY, 60_000L);
		var refreshSvc = new RefreshTokenGenerateServiceImpl(
				SECRET_KEY,
				60_000L,
				new InMemoryRefreshTokenRepository()
		);
		var signupSvc  = new SignupTokenGenerateServiceImpl(SECRET_KEY, 60_000L);
		var refreshTokenSvc = mock(RefreshTokenService.class);

		// 2) JwtUtils 인스턴스 생성
		var realJwtUtils = new JwtUtils(
				SECRET_KEY, accessSvc, refreshSvc, signupSvc, refreshTokenSvc
		);

		// 3) 테스트용 Users
		Users user = Users.builder()
				.provider(Provider.kakao)
				.providerId("subject123")
				.build();

		// 4) 액세스 토큰 생성
		String token = realJwtUtils.generateAccessToken(user);

		// 5) 파싱
		Claims claims = realJwtUtils.parseClaims(token);

		// subject (providerId) 검증
		assertEquals("subject123", claims.getSubject());

		// custom claim 검증
		assertEquals(
				user.getVerificationStatus().name(),
				claims.get("verificationStatus", String.class)
		);
		assertEquals(
				"auth",
				claims.get("tokenType", String.class)
		);
	}

	@Test
	void extractAllClaimsAsMap_ReturnsCorrectMap() {
		// real instance 재사용
		var accessSvc  = new AccessTokenGenerateServiceImpl(SECRET_KEY, 60_000L);
		var refreshSvc = new RefreshTokenGenerateServiceImpl(
				SECRET_KEY,
				60_000L,
				new InMemoryRefreshTokenRepository()
		);
		var signupSvc  = new SignupTokenGenerateServiceImpl(SECRET_KEY, 60_000L);
		var refreshTokenSvc = mock(RefreshTokenService.class);

		var realJwtUtils = new JwtUtils(
				SECRET_KEY, accessSvc, refreshSvc, signupSvc, refreshTokenSvc
		);

		Users user = Users.builder()
				.provider(Provider.kakao)
				.providerId("multiClaimsUser")
				.build();

		String token = realJwtUtils.generateAccessToken(user);
		Map<String, Object> map = realJwtUtils.extractAllClaimsAsMap(token);

		// 기본 클레임(subject)은 map.get(Claims.SUBJECT)
		assertEquals("multiClaimsUser", map.get(Claims.SUBJECT));
		assertEquals(
				user.getVerificationStatus().name(),
				map.get("verificationStatus")
		);
		assertEquals("auth", map.get("tokenType"));
	}

	@Test
	void validateToken_expired_Throws() throws InterruptedException {
		// 1초 만료 서비스
		var accessSvc  = new AccessTokenGenerateServiceImpl(SECRET_KEY, 1_000L);
		var refreshSvc = new RefreshTokenGenerateServiceImpl(
				SECRET_KEY,
				1_000L,
				new InMemoryRefreshTokenRepository()
		);
		var signupSvc  = new SignupTokenGenerateServiceImpl(SECRET_KEY, 1_000L);
		var refreshTokenSvc = mock(RefreshTokenService.class);

		var shortJwt   = new JwtUtils(
				SECRET_KEY, accessSvc, refreshSvc, signupSvc, refreshTokenSvc
		);

		Users user = Users.builder()
				.provider(Provider.kakao)
				.providerId("expireTest")
				.build();

		String token = shortJwt.generateAccessToken(user);
		Thread.sleep(1_100L);

		assertThrows(InvalidTokenException.class,
				() -> shortJwt.validateToken(token)
		);
	}
}
