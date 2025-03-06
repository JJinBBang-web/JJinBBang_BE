package JJinBBang.app.global.jwt;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import JJinBBang.app.global.common.enums.Provider;
import JJinBBang.app.global.security.SecurityPathProperties;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.service.UsersService;
import JJinBBang.app.global.common.enums.VerificationStatus;
import JJinBBang.app.global.error.exception.NotFoundGroupException;
import JJinBBang.app.global.jwt.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtils jwtUtils;
	private final UsersService usersService;
	private final SecurityPathProperties securityPathProperties;
	private final AntPathMatcher pathMatcher = new AntPathMatcher(); // Ant 패턴 매칭 객체

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
				String tokenType = claims.get("tokenType", String.class);
				Users user;

				String requestURI = request.getRequestURI();
				boolean isPendingUserPath = securityPathProperties.getPendingUser().stream()
						.anyMatch(pattern -> pathMatcher.match(pattern, requestURI)); // AntPathMatcher 사용

				if(tokenType.equals("signup")){
					// 회원가입용 토큰인 경우
					if (!isPendingUserPath) {
						throw new InvalidTokenException("회원가입 토큰은 회원가입 시에만 사용할 수 있습니다.");
					}

					String provider = claims.get("provider", String.class);

					user = Users.builder()
						.providerId(providerId)
						.provider(Provider.valueOf(provider))
						.build();
				} else if (tokenType.equals("auth")) {
					// 인증용 토큰인 경우
					if (isPendingUserPath) {
						throw new InvalidTokenException("인증 토큰은 회원가입 시에 사용할 수 없습니다.");
					}

					VerificationStatus status = VerificationStatus.valueOf(claims.get("verificationStatus", String.class));

					// 4. DB에서 유저 정보 조회
					user = usersService.findByProviderId(providerId);
				}
				else {
					throw new InvalidTokenException("유효하지 않은 토큰 타입입니다.");
				}

				// 5. SecurityContextHolder에 인증 정보 저장
				var authentication = new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
			// 6. 필터 체인 진행
			filterChain.doFilter(request, response);

		} catch (InvalidTokenException | NotFoundGroupException e) {
			makeErrorResponse(e, response);
		}
	}

	private void makeErrorResponse(Exception e, HttpServletResponse response) throws IOException {
		log.error(e.getMessage());
		response.setContentType("application/json;charset=UTF-8");
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> errorDetails = new HashMap<>();

		if(e instanceof InvalidTokenException){
			// 토큰 관련 예외 처리
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

			errorDetails.put("code", HttpServletResponse.SC_UNAUTHORIZED);
			errorDetails.put("message", e.getMessage());
		}
		else if(e instanceof NotFoundGroupException){
			// 유저 관련 예외 처리
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);

			errorDetails.put("code", HttpServletResponse.SC_NOT_FOUND);
			errorDetails.put("message", e.getMessage());
		}

		response.getWriter().write(mapper.writeValueAsString(errorDetails));
	}
}
