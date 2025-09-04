package JJinBBang.app.global.jwt;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import JJinBBang.app.global.common.enums.Provider;
import JJinBBang.app.global.jwt.enums.TokenType;
import JJinBBang.app.global.security.SecurityPathMatchUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


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
	private final AuthenticationEntryPoint authenticationEntryPoint; // 401 예외 핸들러
	private final SecurityPathMatchUtil securityPathMatchUtil;

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
				// 유저
				Users user;
				// 유저의 권한
				List<GrantedAuthority> authorities = Collections.emptyList();

				boolean isPendingUserPath = securityPathMatchUtil.pendingUserMatch(request);
				boolean isRefreshPath = securityPathMatchUtil.refreshTokenMatch(request);

				if(tokenType.equals(TokenType.SIGNUP.getType())){
					// 회원가입용 토큰인 경우
					if (!isPendingUserPath) {
						throw InvalidTokenException.signupTokenNotAllowed();
					}

					String provider = claims.get("provider", String.class);

					user = Users.builder()
						.providerId(providerId)
						.provider(Provider.valueOf(provider))
						.build();
				} else if (tokenType.equals(TokenType.ACCESS.getType()) ||
						tokenType.equals(TokenType.REFRESH.getType())
				) {
					// 인증용 토큰인 경우
					if (isPendingUserPath) {
						throw InvalidTokenException.authTokenNotAllowed();
					}
					if(tokenType.equals(TokenType.ACCESS.getType()) && isRefreshPath) {
						// Access Token인 경우
						throw InvalidTokenException.accessTokenNotAllowed();
					}
					if(tokenType.equals(TokenType.REFRESH.getType()) && !isRefreshPath) {
						// Refresh Token인 경우
						throw InvalidTokenException.refreshTokenNotAllowed();
					}

					VerificationStatus status = VerificationStatus.valueOf(claims.get("verificationStatus", String.class));

					// 4. DB에서 유저 정보 조회
					user = usersService.findByProviderId(providerId);
					// 유저 권한 설정
					authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
				}
				else {
					throw new InvalidTokenException("유효하지 않은 토큰 타입입니다.");
				}

				// 탈퇴한 유저 확인
				if (user.getDisabledAt() != null && user.getDisabledAt().isBefore(LocalDateTime.now())) {
					throw InvalidTokenException.deletedUser();
				}

				// 5. SecurityContextHolder에 인증 정보 저장
				var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
			// 6. 필터 체인 진행
			filterChain.doFilter(request, response);

		} catch (InvalidTokenException e){
			authenticationEntryPoint.commence(request , response, e);
		} catch (NotFoundGroupException e) {
			authenticationEntryPoint.commence(request , response, InvalidTokenException.userNotFound());
		}
	}
}
