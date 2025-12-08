package JJinBBang.app.global.security.resolver;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest.Builder;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 카카오 로그인 시 계정 선택 화면을 표시하기 위한 커스텀 OAuth2 Authorization Request Resolver
 * prompt=select_account 파라미터를 추가하여 사용자가 매번 계정을 선택할 수 있도록 함
 */
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

	private final OAuth2AuthorizationRequestResolver defaultResolver;

	public CustomOAuth2AuthorizationRequestResolver(
			ClientRegistrationRepository clientRegistrationRepository, String authorizationRequestBaseUri) {
		this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
				clientRegistrationRepository, authorizationRequestBaseUri);
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
		OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
		return customizeAuthorizationRequest(authorizationRequest, request);
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
		OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
		return customizeAuthorizationRequest(authorizationRequest, request, clientRegistrationId);
	}

	private OAuth2AuthorizationRequest customizeAuthorizationRequest(
			OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request) {
		return customizeAuthorizationRequest(authorizationRequest, request, null);
	}

	private OAuth2AuthorizationRequest customizeAuthorizationRequest(
			OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, String clientRegistrationId) {
		if (authorizationRequest == null) {
			return null;
		}

		// registration_id 추출: 파라미터에서 가져오거나 URI에서 추출
		String registrationId = clientRegistrationId;
		if (registrationId == null) {
			// URI에서 추출: /api/v1/auth/signIn/kakao 형태
			String requestURI = request.getRequestURI();
			String[] parts = requestURI.split("/");
			if (parts.length > 0) {
				registrationId = parts[parts.length - 1];
			}
		}

		// 카카오 로그인인 경우에만 prompt 파라미터 추가
		if ("kakao".equals(registrationId)) {
			Map<String, Object> additionalParameters = new HashMap<>(
					authorizationRequest.getAdditionalParameters());
			// prompt=select_account를 추가하여 계정 선택 화면 표시
			additionalParameters.put("prompt", "select_account");

			Builder builder = OAuth2AuthorizationRequest.from(authorizationRequest)
					.additionalParameters(additionalParameters);

			return builder.build();
		}

		return authorizationRequest;
	}
}
