package JJinBBang.app.global.security.filter;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.security.SecurityPathProperties;
import JJinBBang.app.global.security.exception.SecurityAccessDeniedException;
import JJinBBang.app.global.security.exception.SecurityAuthException;
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

@Component
@RequiredArgsConstructor
public class PendingUserFilter extends OncePerRequestFilter {
    private final SecurityPathProperties securityPathProperties;
    private final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        List<String> pendingUserPaths = securityPathProperties.getPendingUser();
        String requestURI = request.getRequestURI();

        boolean isMatch = pendingUserPaths.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, requestURI));

        if (isMatch) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 인증이 없을 경우 401 Unauthorized 반환
            if (authentication == null || !(authentication.getPrincipal() instanceof Users user)) {
                makeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, SecurityAuthException.noAuthentication());
                return;
            }


            // 아래 오류는 JWT 필터에서 이미 처리되었을 것.
            // 회원가입 URL인데 이미 유저 아이디가 존재하는 경우
            // 회원가입 URL이 아닌데 유저 아이디가 없는 경우
            // -> 403 Forbidden
            if (user.getUserId() != null) {
                makeErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, SecurityAccessDeniedException.wrongAccess());
                return;
            }
        }


        // 검증 성공 시 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }

    /**
     * JSON 형태로 오류 응답을 반환하는 유틸리티 메서드
     */
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
