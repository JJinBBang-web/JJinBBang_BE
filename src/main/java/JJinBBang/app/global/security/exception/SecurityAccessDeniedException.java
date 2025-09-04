package JJinBBang.app.global.security.exception;

import org.springframework.security.access.AccessDeniedException;

public class SecurityAccessDeniedException extends AccessDeniedException {

    public SecurityAccessDeniedException(String message) {
        super(message);
    }

    public static SecurityAccessDeniedException alreadyUniversityVerified(){
        return new SecurityAccessDeniedException("이미 학교 인증이 완료되었습니다.");
    }

    public static SecurityAccessDeniedException universityVerificationRequired() {
        return new SecurityAccessDeniedException("학교 인증이 필요합니다.");
    }

    public static SecurityAccessDeniedException emailVerificationRequired() {
        return new SecurityAccessDeniedException("이메일 인증이 필요합니다.");
    }

    public static SecurityAccessDeniedException enrollStudentVerificationRequired() {
        return new SecurityAccessDeniedException("재학생 인증이 필요합니다.");
    }

    public static SecurityAccessDeniedException newStudentVerificationRequired() {
        return new SecurityAccessDeniedException("신입생 인증이 필요합니다.");
    }

    public static SecurityAccessDeniedException pendingUnivVerifyRequestRequired() {
        return new SecurityAccessDeniedException("학교 인증 요청 대기가 필요합니다.");
    }

    public static SecurityAccessDeniedException unverifiedStatusRequired() {
        return new SecurityAccessDeniedException("미인증 상태여야 합니다.");
    }

    public static SecurityAccessDeniedException existPendingUnivVerifyRequest() {
        return new SecurityAccessDeniedException("대기 중인 학교 인증 요청이 존재합니다.");
    }

    public static SecurityAccessDeniedException pendingUnivVerifyRequestNotFound() {
        return new SecurityAccessDeniedException("학교 인증 요청이 없습니다.");
    }

    public static SecurityAccessDeniedException wrongAccess() {
        return new SecurityAccessDeniedException("잘못된 접근입니다.");
    }

    public static SecurityAccessDeniedException noAuthority() {
        return new SecurityAccessDeniedException("접근 권한이 없습니다.");
    }
}
