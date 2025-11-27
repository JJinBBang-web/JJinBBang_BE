package JJinBBang.app.global.util;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.service.UsersService;
import JJinBBang.app.global.common.enums.Provider;
import JJinBBang.app.global.common.enums.VerificationStatus;
import JJinBBang.app.global.jwt.service.impl.AccessTokenGenerator;
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
	private AccessTokenGenerator accessTokenGenerator;

	@Test
	@DisplayName("더미 계정 생성 후 Access Token 발급 (기본 UNVERIFIED 상태)")
	void generateAccessTokenForDummyUser() {
		generateAccessTokenForDummyUserWithVerificationStatus(VerificationStatus.UNVERIFIED);
	}

	@Test
	@DisplayName("더미 계정 생성 후 Access Token 발급 (VERIFIED 상태)")
	void generateAccessTokenForVerifiedUser() {
		generateAccessTokenForDummyUserWithVerificationStatus(VerificationStatus.VERIFIED);
	}

	@Test
	@DisplayName("더미 계정 생성 후 Access Token 발급 (EMAIL_VERIFIED 상태)")
	void generateAccessTokenForEmailVerifiedUser() {
		generateAccessTokenForDummyUserWithVerificationStatus(VerificationStatus.EMAIL_VERIFIED);
	}

	@Test
	@DisplayName("더미 계정 생성 후 Access Token 발급 (ENROLL_STUDENT_VERIFIED 상태)")
	void generateAccessTokenForEnrollStudentVerifiedUser() {
		generateAccessTokenForDummyUserWithVerificationStatus(VerificationStatus.ENROLL_STUDENT_VERIFIED);
	}

	@Test
	@DisplayName("더미 계정 생성 후 Access Token 발급 (NEW_STUDENT_VERIFIED 상태)")
	void generateAccessTokenForNewStudentVerifiedUser() {
		generateAccessTokenForDummyUserWithVerificationStatus(VerificationStatus.NEW_STUDENT_VERIFIED);
	}

	@Test
	@DisplayName("더미 계정 생성 후 Access Token 발급 (PENDING 상태)")
	void generateAccessTokenForPendingUser() {
		generateAccessTokenForDummyUserWithVerificationStatus(VerificationStatus.PENDING);
	}

	private void generateAccessTokenForDummyUserWithVerificationStatus(VerificationStatus verificationStatus) {
		// --- 1) 더미 계정 생성 및 저장 ---
		String providerId = "test-dummy-user-" + System.currentTimeMillis();
		Users saved;

		if (usersService.existsByProviderId(providerId)) {
			saved = usersService.findByProviderId(providerId);
		} else {
			Users user = Users.builder()
					.provider(Provider.kakao)
					.providerId(providerId)
					.build();
			saved = usersService.save(user);
		}

		assertNotNull(saved.getUserId(), "저장된 유저는 userId가 있어야 한다");

		// --- 2) 인증 상태 수정 (필요한 경우) ---
		if (saved.getVerificationStatus() != verificationStatus) {
			saved.updateVerificationStatus(verificationStatus);
			saved = usersService.save(saved);
		}

		// --- 3) Access Token 발급 ---
		String accessToken = accessTokenGenerator.generate(saved);
		assertNotNull(accessToken);
		assertFalse(accessToken.isBlank());

		System.out.println("==========================================");
		System.out.println("더미 계정 정보:");
		System.out.println("  - User ID: " + saved.getUserId());
		System.out.println("  - Provider: " + saved.getProvider());
		System.out.println("  - Provider ID: " + saved.getProviderId());
		System.out.println("  - Role: " + saved.getRole());
		System.out.println("  - Verification Status: " + saved.getVerificationStatus());
		System.out.println("==========================================");
		System.out.println("Access Token:");
		System.out.println(accessToken);
		System.out.println("==========================================");
	}
}
