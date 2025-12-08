package JJinBBang.app.domain.user.entity.oauth;

import java.util.HashMap;
import java.util.Map;

public class GoogleUserInfo implements OAuth2UserInfo {
	private final Map<String, Object> attrs;
	private static final String PROVIDER_ID_KEY = "sub";

	public GoogleUserInfo(Map<String, Object> attrs) {
		this.attrs = new HashMap<>(attrs);
	}

	@Override
	public String getProviderId() {
		// 구글의 고유 사용자 ID는 "sub" 필드에 들어있음
		return String.valueOf(attrs.get(PROVIDER_ID_KEY));
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attrs;
	}
}
