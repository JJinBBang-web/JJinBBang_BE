package JJinBBang.app.global.security.filter;

import static JJinBBang.app.global.cookie.CookieType.*;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

import JJinBBang.app.domain.user.service.UsersService;
import JJinBBang.app.global.cookie.CookieUtils;
import JJinBBang.app.global.jwt.service.JwtService;
import JJinBBang.app.global.security.exception.SecurityAuthException;
import JJinBBang.app.global.security.exception.SecurityErrorResponder;
import JJinBBang.app.global.security.redirect.OAuthRedirectCookieManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginShortcutFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UsersService usersService;
	private final CookieUtils cookieUtils;
	private final SecurityErrorResponder responder;
	private final OAuthRedirectCookieManager redirectCookieManager;

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		String uri = req.getRequestURI();
		// OAuth2 시작 엔드포인트로 가는 요청만 개입
		if (!uri.startsWith("/api/v1/auth/signIn/")) {
			chain.doFilter(req, res);
			return;
		}

		String frontendRedirect;
		try {
			frontendRedirect = redirectCookieManager.resolveAndStore(req, res);
		} catch (SecurityAuthException e) {
			log.warn("redirect 파라미터 검증 실패", e);
			responder.write(res, e, HttpStatus.BAD_REQUEST);
			return;
		}

		String refresh = getCookie(req, REFRESH_TOKEN_COOKIE);
		String pending = getCookie(req, PENDING_TOKEN_COOKIE);

		// 1) 약관 진행 중이면 약관 페이지로 바로 안내
		if (pending != null && !pending.isBlank()) {
			redirectWith(res, frontendRedirect, "terms_pending");
			return;
		}

		// 2) 리프레시 토큰이 유효하면 “이미 로그인됨”으로 단축
		if (refresh != null && !refresh.isBlank()) {
			try {
				var claims = jwtService.parseClaims(refresh);
				Long userId = Long.valueOf(claims.getSubject());
				usersService.findById(userId); // 존재 확인
				redirectWith(res, frontendRedirect, "success");
				return;
			} catch (Exception e) {
				// 만료/위조 등 → 쿠키만 정리하고 실제 OAuth2 시도로 진행
				cookieUtils.deleteCookie(res, REFRESH_TOKEN_COOKIE);
			}
		}

		// 3) 여기까지 왔으면 실제 OAuth2 로그인 플로우로 진행
		chain.doFilter(req, res);
	}

	private static String getCookie(HttpServletRequest req, String name) {
		if (req.getCookies() == null)
			return null;
		for (var c : req.getCookies())
			if (name.equals(c.getName()))
				return c.getValue();
		return null;
	}

	private static void redirectWith(HttpServletResponse res, String frontend, String status)
			throws IOException {
		var b = UriComponentsBuilder.fromUriString(frontend).queryParam("status", status);
		res.sendRedirect(b.build().toUriString());
	}
}
