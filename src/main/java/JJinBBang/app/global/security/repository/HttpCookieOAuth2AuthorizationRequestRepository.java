package JJinBBang.app.global.security.repository;

import static JJinBBang.app.global.cookie.CookieType.*;

import java.util.Base64;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import JJinBBang.app.global.cookie.CookieUtils;
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
		cookieUtils.addCookie(response, OAUTH2_AUTH_REQUEST_COOKIE,
			cookieUtils.serialize(authorizationRequest), 300);
		String redirect = request.getParameter("redirect");
		if (redirect != null) {
			cookieUtils.addCookie(response, REDIRECT_URI_PARAM_COOKIE,
				Base64.getUrlEncoder().encodeToString(redirect.getBytes()), 300);
		}
	}

	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
		HttpServletResponse response) {
		return loadAuthorizationRequest(request);
	}
}
