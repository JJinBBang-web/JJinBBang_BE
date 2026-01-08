package JJinBBang.app.domain.user.entity.oauth;

import java.util.HashMap;
import java.util.Map;

public class KakaoUserInfo implements OAuth2UserInfo {
	private final Map<String, Object> attrs;
	private static final String PROVIDER_ID_KEY = "id";

	public KakaoUserInfo(Map<String, Object> attrs) {
		this.attrs = new HashMap<>(attrs);
	}

	@Override
	public String getProviderId() {
		return String.valueOf("kakao_" + attrs.get(PROVIDER_ID_KEY));
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attrs;
	}
}