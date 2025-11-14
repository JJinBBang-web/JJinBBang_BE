package JJinBBang.app.global.cookie;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt.cookie")
public class CookieProperties {
	/**
	 * 쿠키 Domain (예: .example.com)
	 * 개발(localhost)에서는 보통 비워둔다.
	 */
	private String domain;

	/**
	 * Secure 플래그 (HTTPS 전용)
	 * 개발에서는 false, 운영에서는 true
	 */
	private boolean secure;

	/**
	 * HttpOnly 여부 (JS 접근 차단)
	 */
	private boolean httpOnly = true;

	/**
	 * SameSite 정책 (None | Lax | Strict)
	 */
	private String sameSite = "Lax";
}
