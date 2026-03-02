package JJinBBang.app.global.common.enums;

public enum VerificationStatus {
	VERIFIED("인증완료"), // 포괄적 인증 완료
	NEW_STUDENT_VERIFIED("인증완료"), // 합격증명서 인증
	ENROLL_STUDENT_VERIFIED("인증완료"), // 재학증명서 인증
	EMAIL_VERIFIED("인증완료"), // 학교 웹메일 인증
	PENDING("인증대기"), // 인증대기
	UNVERIFIED("미인증"), // 미인증
	REJECTED("반려"); // 반려

	private final String status;

	VerificationStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public static boolean isValid(String status) {
		for (VerificationStatus s : values()) {
			if (s.name().equalsIgnoreCase(status)) {
				return true;
			}
		}
		return false;
	}
}
