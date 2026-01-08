package JJinBBang.app.domain.user.entity.oauth;

import java.util.Map;

public interface OAuth2UserInfo {
	String getProviderId();     // 각 사의 고유키
	Map<String, Object> getAttributes();
}
