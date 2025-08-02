package JJinBBang.app.global.security.filter;

import JJinBBang.app.global.common.enums.VerificationStatus;
import JJinBBang.app.global.security.SecurityPathProperties;
import JJinBBang.app.global.security.exception.SecurityAccessDeniedException;
import JJinBBang.app.global.security.exception.SecurityAuthException;
import JJinBBang.app.domain.user.entity.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 특정 경로에서 사용자의 verificationStatus(이메일 인증 상태)를 확인하는 필터
 * - verificationStatus가 맞지 않으면 403 Forbidden 응답을 반환
 */
@Component
@RequiredArgsConstructor
public class VerificationStatusFilter extends OncePerRequestFilter {

    private final SecurityPathProperties securityPathProperties;
    private final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Map<String, List<String>> verificationStatusPaths = securityPathProperties.getVerificationStatusBased();
        String requestURI = request.getRequestURI();


        // verificationStatus 확인
        for (Map.Entry<String, List<String>> entry : verificationStatusPaths.entrySet()) {
            VerificationStatus requiredStatus = VerificationStatus.valueOf(entry.getKey());

            boolean isMatch = entry.getValue().stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, requestURI));

            if (isMatch) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                // 인증이 없을 경우 401 Unauthorized 반환
                if (authentication == null || !(authentication.getPrincipal() instanceof Users user)) {
                    makeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, SecurityAuthException.noAuthentication());
                    return;
                }

                if(!isValidVerificationStatus(user.getVerificationStatus(), requiredStatus)) {
                    makeErrorResponse(response, HttpServletResponse.SC_FORBIDDEN,
                            getCustomSecurityAuthException(requiredStatus));
                    return;
                }
            }
        }

        // 검증 성공 시 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }

    private boolean isValidVerificationStatus(VerificationStatus userStatus, VerificationStatus requiredStatus) {
		if (requiredStatus.equals(VerificationStatus.VERIFIED)) {
			return userStatus.equals(VerificationStatus.EMAIL_VERIFIED) ||
				userStatus.equals(VerificationStatus.ENROLL_STUDENT_VERIFIED) ||
				userStatus.equals(VerificationStatus.NEW_STUDENT_VERIFIED);
		}
		return userStatus.equals(requiredStatus);
	}


    /**
     * 현재 verificationStatus와 필요한 verificationStatus를 비교하여 예외 메시지 생성
     */
    private AuthenticationException getCustomSecurityAuthException(VerificationStatus requiredStatus) {
        // userStatus와 requiredStatus 매칭이 유효하지 않은 경우에만 함수 실행

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
                return SecurityAuthException.noPermission();
            }
        }
    }


    private void makeErrorResponse(HttpServletResponse response, int status, AuthenticationException exception) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("code", status);
        errorDetails.put("message", exception.getMessage());
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(errorDetails));
    }
}
