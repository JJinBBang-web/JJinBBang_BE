package JJinBBang.app.global.jwt;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.service.UsersService;
import JJinBBang.app.global.common.enums.VerificationStatus;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtils jwtUtils;
	private final UsersService usersService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		try {
			// 1. 토큰 추출
			String token = jwtUtils.extractToken(request);

			// 2. 토큰 존재 및 검증
			if (token != null && jwtUtils.validateToken(token)) {
				// 3. 토큰에서 클레임 추출
				Claims claims = jwtUtils.parseClaims(token);
				String providerId = claims.getSubject();
				VerificationStatus status = VerificationStatus.valueOf(claims.get("verificationStatus", String.class));

				// 4. DB에서 유저 정보 조회
				Users user = usersService.findByProviderId(providerId);
				if (user == null) {
					throw new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다.");
				}

				// 5. SecurityContextHolder에 인증 정보 저장
				var authentication = new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
			// 6. 필터 체인 진행
			filterChain.doFilter(request, response);

		} catch (Exception e) {
			// 7. 예외 처리 (필요 시 전역 예외 처리기로 전달 가능)
			// TODO: 예외 응답 형식 변경해야 함
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
		}
	}


	private boolean validateToken(String token) {
		try {
			jwtUtils.validateToken(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
