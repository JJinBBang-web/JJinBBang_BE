package JJinBBang.app.domain.user.entity.oauth;

import java.util.Map;

@SuppressWarnings("unchecked")
public class NaverUserInfo implements OAuth2UserInfo {
	private final Map<String, Object> attrs;
	private final Map<String, Object> response;

	public NaverUserInfo(Map<String, Object> attrs) {
		this.attrs = attrs;
		// 네이버 응답 구조상 "response" 키 아래에 실제 유저 정보가 있음
		this.response = (Map<String, Object>) attrs.get("response");
	}

	@Override
	public String getProviderId() {
		// 네이버의 고유 사용자 ID
		return String.valueOf(response.get("id"));
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attrs;
	}

	public String getEmail() {
		return (String) response.get("email");
	}

	public String getName() {
		return (String) response.get("name");
	}

	public String getProfileImage() {
		return (String) response.get("profile_image");
	}
}
