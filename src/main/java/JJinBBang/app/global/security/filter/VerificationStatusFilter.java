package JJinBBang.app.global.security.filter;

import JJinBBang.app.global.common.enums.VerificationStatus;
import JJinBBang.app.global.security.SecurityPathMatcher;
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
    private final SecurityPathMatcher securityPathMatcher;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        System.out.println("requestURI = " + requestURI);
        String method = request.getMethod();
        System.out.println("method = " + method);

        Map<String, Map<String, List<String>>> verificationStatusPaths = securityPathProperties.getVerificationStatusBased();

        // verificationStatus 확인
        for (Map.Entry<String, Map<String, List<String>>> entry : verificationStatusPaths.entrySet()) {
            // 필요한 학교 인증 상태
            VerificationStatus requiredStatus = VerificationStatus.valueOf(entry.getKey());

            // 현재 순회하는 학교인증상태 경로 중에서 요청 method에 해당하는 경로들
            boolean isMatch = securityPathMatcher.match(requestURI, method, entry.getValue());

            if (isMatch) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                // 인증이 없을 경우
                if (authentication == null || !(authentication.getPrincipal() instanceof Users user)) {
                    authenticationEntryPoint.commence(request, response, SecurityAuthException.noAuthentication());
                    return;
                }

                AccessDeniedException exception = getCustomSecurityAuthException(
                    user.getVerificationStatus(), requiredStatus);
                if(exception != null) {
                    accessDeniedHandler.handle(request, response, exception);
                    return;
                }
            }
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
