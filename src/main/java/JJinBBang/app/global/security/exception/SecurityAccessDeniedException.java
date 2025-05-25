package JJinBBang.app.global.security.exception;


import org.springframework.security.core.AuthenticationException;

public class SecurityAccessDeniedException extends AuthenticationException {

    public SecurityAccessDeniedException(String message) {
        super(message);
    }

    public static SecurityAccessDeniedException alreadyUniversityVerified(){
        return new SecurityAccessDeniedException("이미 학교 인증이 완료되었습니다.");
    }

    public static SecurityAccessDeniedException universityVerificationRequired() {
        return new SecurityAccessDeniedException("학교 인증이 필요합니다.");
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
}
