package JJinBBang.app.global.common.enums;

public enum VerificationStatus {
	NEW_STUDENT_VERIFIED("인증완료"), // 합격증명서 인증
	ENROLL_STUDENT_VERIFIED("인증완료"), // 재학증명서 인증
	EMAIL_VERIFIED("인증완료"), // 학교 웹메일 인증
	PENDING("인증대기"), // 인증대기
	UNVERIFIED("미인증"); // 미인증

	private final String status;

	VerificationStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}
}
