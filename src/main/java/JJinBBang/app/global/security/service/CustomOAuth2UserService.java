package JJinBBang.app.global.security.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import JJinBBang.app.domain.user.entity.oauth.GoogleUserInfo;
import JJinBBang.app.domain.user.entity.oauth.KakaoUserInfo;
import JJinBBang.app.domain.user.entity.oauth.NaverUserInfo;
import JJinBBang.app.domain.user.entity.oauth.OAuth2UserInfo;
import JJinBBang.app.global.common.enums.Provider;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) {
		OAuth2User oAuth2User = super.loadUser(userRequest);
		String registrationId = userRequest.getClientRegistration().getRegistrationId(); // kakao/google/naver
		Provider provider = Provider.valueOf(registrationId.toLowerCase());

		OAuth2UserInfo ui = of(provider, oAuth2User.getAttributes());
		String providerId = ui.getProviderId();

		Map<String, Object> standard = new HashMap<>(ui.getAttributes());
		standard.put("provider", provider.name());
		standard.put("providerId", providerId);

		return new DefaultOAuth2User(Set.of(() -> "ROLE_ANONYMOUS"), standard, "providerId");
	}

	public static OAuth2UserInfo of(Provider provider, Map<String, Object> attributes) {
		return switch (provider) {
			case kakao -> new KakaoUserInfo(attributes);
			case google -> new GoogleUserInfo(attributes);
			case naver -> new NaverUserInfo(attributes);
		};
	}
}
