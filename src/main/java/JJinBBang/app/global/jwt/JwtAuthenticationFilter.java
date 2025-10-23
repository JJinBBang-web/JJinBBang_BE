package JJinBBang.app.global.jwt;

import static JJinBBang.app.global.jwt.enums.TokenType.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.service.UsersService;
import JJinBBang.app.global.error.exception.NotFoundGroupException;
import JJinBBang.app.global.jwt.enums.TokenType;
import JJinBBang.app.global.jwt.exception.InvalidTokenException;
import JJinBBang.app.global.jwt.service.JwtService;
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

	private final JwtService jwtService;
	private final UsersService usersService;
	private final AuthenticationEntryPoint authenticationEntryPoint; // 401 예외 핸들러

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String authorization = request.getHeader("Authorization");
		if(authorization != null && authorization.startsWith("Bearer ")) {
			String token = jwtService.extractBearerTokenFromHeader(authorization);
			try {
				Claims claims = jwtService.parseClaims(token);

				String tokenType = claims.get("tokenType", String.class);
				if(ACCESS != TokenType.valueOf(tokenType)) {
					throw InvalidTokenException.invalidToken();
				}

				Long userId = Long.valueOf(claims.getSubject());
				Users user = usersService.findById(userId);

				if (user.getDisabledAt() != null && user.getDisabledAt().isBefore(LocalDateTime.now())) {
					throw InvalidTokenException.deletedUser();
				}

				List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

				var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}  catch (InvalidTokenException e){
				authenticationEntryPoint.commence(request , response, e);
			} catch (NotFoundGroupException e) {
				authenticationEntryPoint.commence(request , response, InvalidTokenException.userNotFound());
			}
		}
		filterChain.doFilter(request, response);
	}
}
