package JJinBBang.app.global.security.handler;

import static JJinBBang.app.global.cookie.CookieType.*;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import JJinBBang.app.domain.user.entity.PendingUser;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.repository.PendingUserRepository;
import JJinBBang.app.domain.user.repository.UsersRepository;
import JJinBBang.app.global.common.enums.Provider;
import JJinBBang.app.global.cookie.CookieUtils;
import JJinBBang.app.global.jwt.dto.TokenPair;
import JJinBBang.app.global.jwt.service.JwtService;
import JJinBBang.app.global.security.service.OAuthRedirectCookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final PendingUserRepository pendingRepo;
	private final UsersRepository userRepository;
	private final JwtService jwtService;
	private final CookieUtils cookieUtils;
	private final OAuthRedirectCookieService redirectCookieManager;

	@Value("${jwt.expiration-time.refresh-token}")
	private int refreshTTLMilli;

	@Value("${jwt.expiration-time.signup-token}")
	private int pendingTTLMilli;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		var oAuthToken = (OAuth2AuthenticationToken) authentication;
		OAuth2User principal = oAuthToken.getPrincipal();

		String registrationId = principal.getAttribute("provider"); // "kakao" 등
		Provider provider = Provider.valueOf(registrationId);
		String providerId = principal.getAttribute("providerId");

		String frontendRedirect = redirectCookieManager.resolveFromCookie(request);

		Optional<Users> userOpt = userRepository.findByProviderId(providerId);

		if (userOpt.isEmpty()) {
			// 최초 로그인 (약관동의 미완료 회원): DB 생성 금지 → Pending 발급
			PendingUser pending = new PendingUser(
					UUID.randomUUID().toString(), provider, providerId, Instant.now().plusMillis(pendingTTLMilli));
			pendingRepo.save(pending);
			cookieUtils.addCookie(response, PENDING_TOKEN_COOKIE, pending.pendingId(), pendingTTLMilli);

			String target = UriComponentsBuilder.fromUriString(frontendRedirect)
					.queryParam("status", "terms_pending")
					.build().toUriString();
			clearAuthCookies(response);
			cookieUtils.deleteCookie(response, REDIRECT_URI_PARAM_COOKIE);
			response.sendRedirect(target);
		} else {
			Users user = userOpt.get();
			TokenPair pair = jwtService.generateTokenPair(user);
			cookieUtils.addCookie(response, REFRESH_TOKEN_COOKIE, pair.refreshToken(), refreshTTLMilli);

			String target = UriComponentsBuilder.fromUriString(frontendRedirect)
					.queryParam("status", "success")
					.build().toUriString();

			// 로그인 성공 시 쿠키 정리
			clearAuthCookies(response);
			cookieUtils.deleteCookie(response, PENDING_TOKEN_COOKIE);
			response.sendRedirect(target);
		}
	}

	private void clearAuthCookies(HttpServletResponse res) {
		cookieUtils.deleteCookie(res, OAUTH2_AUTH_REQUEST_COOKIE);
		cookieUtils.deleteCookie(res, REDIRECT_URI_PARAM_COOKIE);
	}
}
