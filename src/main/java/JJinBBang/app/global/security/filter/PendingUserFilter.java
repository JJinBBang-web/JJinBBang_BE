package JJinBBang.app.global.security.filter;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.security.SecurityPathMatchUtil;
import JJinBBang.app.global.security.exception.SecurityAccessDeniedException;
import JJinBBang.app.global.security.exception.SecurityAuthException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class PendingUserFilter extends OncePerRequestFilter {
    private final AuthenticationEntryPoint authenticationEntryPoint; // 401 예외 핸들러
    private final AccessDeniedHandler accessDeniedHandler; // 403 예외 핸들러
    private final SecurityPathMatchUtil securityPathMatchUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (securityPathMatchUtil.pendingUserMatch(request)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 인증이 없을 경우 401 Unauthorized 반환
            if (authentication == null || !(authentication.getPrincipal() instanceof Users user)) {
                authenticationEntryPoint.commence(request, response, SecurityAuthException.noAuthentication());
                return;
            }

            // 아래 오류는 JWT 필터에서 이미 처리되었을 것.
            // 회원가입 URL인데 이미 유저 아이디가 존재하는 경우
            // 회원가입 URL이 아닌데 유저 아이디가 없는 경우
            // -> 403 Forbidden
            if (user.getUserId() != null) {
                accessDeniedHandler.handle(request, response, SecurityAccessDeniedException.wrongAccess());
                return;
            }
        }

        // 검증 성공 시 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }
}
