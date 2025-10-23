package JJinBBang.app.domain.user.entity.oauth;

import java.util.Map;

public class KakaoUserInfo implements OAuth2UserInfo {
	private final Map<String, Object> attrs;

	public KakaoUserInfo(Map<String, Object> attrs) {
		this.attrs = attrs;
	}

	@Override
	public String getProviderId() {
		return String.valueOf(attrs.get("id"));
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attrs;
	}
}
