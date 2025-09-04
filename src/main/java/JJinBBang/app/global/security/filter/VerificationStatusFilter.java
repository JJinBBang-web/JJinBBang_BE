package JJinBBang.app.global.security.filter;

import JJinBBang.app.global.common.enums.VerificationStatus;
import JJinBBang.app.global.security.SecurityPathMatchUtil;
import JJinBBang.app.global.security.SecurityPathProperties;
import JJinBBang.app.global.security.exception.SecurityAccessDeniedException;
import JJinBBang.app.global.security.exception.SecurityAuthException;
import JJinBBang.app.domain.user.entity.Users;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 특정 경로에서 사용자의 verificationStatus(이메일 인증 상태)를 확인하는 필터
 * - verificationStatus가 맞지 않으면 403 Forbidden 응답을 반환
 */
@Component
@RequiredArgsConstructor
public class VerificationStatusFilter extends OncePerRequestFilter {

    private final SecurityPathProperties securityPathProperties;
    private final AuthenticationEntryPoint authenticationEntryPoint; // 401 예외 핸들러
    private final AccessDeniedHandler accessDeniedHandler; // 403 예외 핸들러
    private final SecurityPathMatchUtil securityPathMatchUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 이 요청이 어떤 VerificationStatus를 요구하는 경로에 매칭되는지 확인
        VerificationStatus required = securityPathMatchUtil.matchAnyVerificationRequired(request);
        if (required == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Users user)) {
            authenticationEntryPoint.commence(request, response, SecurityAuthException.noAuthentication());
            return;
        }

        AccessDeniedException ex = getCustomSecurityAuthException(user.getVerificationStatus(), required);
        if (ex != null) {
            accessDeniedHandler.handle(request, response, ex);
            return;
        }

        // 검증 성공 시 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }

    private SecurityAccessDeniedException getCustomSecurityAuthException(VerificationStatus userStatus, VerificationStatus requiredStatus) {
        if(isValidVerificationStatus(userStatus, requiredStatus)) {
            // 유효한 상태인 경우
            return null;
        }

        switch (requiredStatus){
            case VERIFIED -> {
                // 유저가 미인증 상태인 경우
                return SecurityAccessDeniedException.universityVerificationRequired();
            }
            case EMAIL_VERIFIED -> {
                // 유저가 이메일 인증을 하지 않은 경우
                return SecurityAccessDeniedException.emailVerificationRequired();
            }
            case ENROLL_STUDENT_VERIFIED -> {
                // 유저가 재학증명서 인증을 하지 않은 경우
                return SecurityAccessDeniedException.enrollStudentVerificationRequired();
            }
            case NEW_STUDENT_VERIFIED -> {
                // 유저가 합격증명서 인증을 하지 않은 경우
                return SecurityAccessDeniedException.newStudentVerificationRequired();
            }
            case PENDING -> {
                // 인증 대기 상태가 필요한 경우
                return SecurityAccessDeniedException.pendingUnivVerifyRequestRequired();
            }
            case UNVERIFIED -> {
                // 미인증 상태여야만 하는 경우
                return SecurityAccessDeniedException.unverifiedStatusRequired();
            }
            default -> {
                return SecurityAccessDeniedException.wrongAccess();
            }
        }
    }

    private boolean isValidVerificationStatus(VerificationStatus userStatus, VerificationStatus requiredStatus) {
        if (requiredStatus.equals(VerificationStatus.VERIFIED)) {
            return userStatus.equals(VerificationStatus.EMAIL_VERIFIED) ||
                userStatus.equals(VerificationStatus.ENROLL_STUDENT_VERIFIED) ||
                userStatus.equals(VerificationStatus.NEW_STUDENT_VERIFIED);
        }
        return userStatus.equals(requiredStatus);
    }
}
