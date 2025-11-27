package JJinBBang.app.global.security.handler;

import static JJinBBang.app.global.cookie.CookieType.OAUTH2_AUTH_REQUEST_COOKIE;
import static JJinBBang.app.global.cookie.CookieType.REDIRECT_URI_PARAM_COOKIE;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import JJinBBang.app.global.cookie.CookieUtils;
import JJinBBang.app.global.security.exception.SecurityAuthException;
import JJinBBang.app.global.security.exception.SecurityErrorResponder;
import JJinBBang.app.global.security.service.OAuthRedirectCookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

	private final CookieUtils cookieUtils;
	private final OAuthRedirectCookieService redirectCookieManager;
	private final SecurityErrorResponder responder;

	@Override
	public void onAuthenticationFailure(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException exception) throws IOException {

		try {
			String redirectUri = redirectCookieManager.resolveFromCookie(request);
			String target = UriComponentsBuilder.fromUriString(redirectUri)
					.queryParam("status", "oauth_failed")
					.build()
					.toUriString();

			clearAuthCookies(response);
			response.sendRedirect(target);
		} catch (SecurityAuthException e) {
			clearAuthCookies(response);
			responder.write(response, e, HttpStatus.BAD_REQUEST);
		}
	}

	private void clearAuthCookies(HttpServletResponse response) {
		cookieUtils.deleteCookie(response, OAUTH2_AUTH_REQUEST_COOKIE);
		cookieUtils.deleteCookie(response, REDIRECT_URI_PARAM_COOKIE);
	}
}
