package JJinBBang.app.global.security.filter;

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
            String requiredStatus = entry.getKey();
            List<String> paths = entry.getValue();

            boolean isMatch = paths.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, requestURI));

            if (isMatch) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                // 인증이 없을 경우 401 Unauthorized 반환
                if (authentication == null || !(authentication.getPrincipal() instanceof Users user)) {
                    makeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, SecurityAuthException.noAuthentication());
                    return;
                }

                if(!user.getVerificationStatus().name().equals(requiredStatus)) {
                    makeErrorResponse(response, HttpServletResponse.SC_FORBIDDEN,
                            getCustomSecurityAuthException(user.getVerificationStatus().name(), requiredStatus));
                    return;
                }
            }
        }

        // 검증 성공 시 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }

    /**
     * 현재 verificationStatus와 필요한 verificationStatus를 비교하여 예외 메시지 생성
     */
    private AuthenticationException getCustomSecurityAuthException(String userStatus, String requiredStatus) {
        if (!userStatus.equals(requiredStatus)) {
            boolean univVerified = "EMAIL_VERIFIED".equals(userStatus) ||
                    "ENROLL_STUDENT_VERIFIED".equals(userStatus) ||
                    "NEW_STUDENT_VERIFIED".equals(userStatus);
            switch (requiredStatus) {
                case "EMAIL_VERIFIED":
                case "ENROLL_STUDENT_VERIFIED":
                case "NEW_STUDENT_VERIFIED":
                    return SecurityAccessDeniedException.universityVerificationRequired();
                case "UNVERIFIED":
                    if (univVerified) {
                        return SecurityAccessDeniedException.alreadyUniversityVerified();
                    } else if ("PENDING".equals(userStatus)) {
                        return SecurityAccessDeniedException.existPendingUnivVerifyRequest();
                    }
                case "PENDING":
                    if (univVerified) {
                        return SecurityAccessDeniedException.alreadyUniversityVerified();
                    } else if ("UNVERIFIED".equals(userStatus)) {
                        return SecurityAccessDeniedException.pendingUnivVerifyRequestNotFound();
                    }
            }
        }
        return SecurityAuthException.noPermission();
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
