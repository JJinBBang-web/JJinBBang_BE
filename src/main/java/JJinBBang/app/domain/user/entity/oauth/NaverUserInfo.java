package JJinBBang.app.domain.user.entity.oauth;

import java.util.HashMap;
import java.util.Map;

public class NaverUserInfo implements OAuth2UserInfo {
	private final Map<String, Object> attrs;
	private static final String PROVIDER_ID_KEY = "id";

	public NaverUserInfo(Map<String, Object> attrs) {
		// 네이버 응답 구조상 "response" 키 아래에 실제 유저 정보가 있음
		Object attr = attrs.get("response");
		if (attr instanceof Map) {
			this.attrs = new HashMap<>((Map<String, Object>) attr);
		} else {
			this.attrs = new HashMap<>();
		}
	}

	@Override
	public String getProviderId() {
		// 네이버의 고유 사용자 ID
		return String.valueOf(attrs.get(PROVIDER_ID_KEY));
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attrs;
	}

	public String getEmail() {
		return (String) attrs.get("email");
	}

	public String getName() {
		return (String) attrs.get("name");
	}

	public String getProfileImage() {
		return (String) attrs.get("profile_image");
	}
}
