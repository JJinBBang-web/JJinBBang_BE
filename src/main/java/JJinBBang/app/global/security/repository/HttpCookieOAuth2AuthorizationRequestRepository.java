package JJinBBang.app.global.security.repository;

import static JJinBBang.app.global.cookie.CookieType.*;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import JJinBBang.app.global.cookie.CookieUtils;
import JJinBBang.app.global.security.service.OAuthRedirectCookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpCookieOAuth2AuthorizationRequestRepository
		implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

	private final CookieUtils cookieUtils;
	private final OAuthRedirectCookieService redirectCookieService;

	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		return cookieUtils.deserialize(request, OAUTH2_AUTH_REQUEST_COOKIE,
				OAuth2AuthorizationRequest.class);
	}

	@Override
	public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
			HttpServletRequest request, HttpServletResponse response) {
		if (authorizationRequest == null) {
			cookieUtils.deleteCookie(response, OAUTH2_AUTH_REQUEST_COOKIE);
			cookieUtils.deleteCookie(response, REDIRECT_URI_PARAM_COOKIE);
			return;
		}

		redirectCookieService.resolveAndStore(request, response);

		// OAuth2 인증 요청 쿠키 만료 시간: 5분 (300,000 밀리초)
		long oauth2RequestCookieTtlMillis = 300_000L;
		cookieUtils.addCookie(response, OAUTH2_AUTH_REQUEST_COOKIE,
				cookieUtils.serialize(authorizationRequest), oauth2RequestCookieTtlMillis);
	}

	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
			HttpServletResponse response) {
		return loadAuthorizationRequest(request);
	}
}
