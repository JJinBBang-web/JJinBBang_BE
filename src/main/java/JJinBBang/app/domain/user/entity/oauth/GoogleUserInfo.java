package JJinBBang.app.domain.user.entity.oauth;

import java.util.Map;

public class GoogleUserInfo implements OAuth2UserInfo {
	private final Map<String, Object> attrs;

	public GoogleUserInfo(Map<String, Object> attrs) {
		this.attrs = attrs;
	}

	@Override
	public String getProviderId() {
		// 구글의 고유 사용자 ID는 "sub" 필드에 들어있음
		return String.valueOf(attrs.get("sub"));
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attrs;
	}
}
