package JJinBBang.app.global.mail.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "mail-auth")
public class MailAuthProperties {
	/**
	 * 이메일 인증 코드 만료 시간 (밀리초)
	 * 주의: application.yml에서 분 단위로 설정되어 있다면, 밀리초로 변환하여 설정해야 합니다.
	 * 예: 5분 = 300,000 밀리초
	 */
	private long expirationTime;

	// mail-auth.auth-code-length
	private int authCodeLength;

	// mail-auth.allowed-domain
	private List<String> allowedDomain;

	// mail-auth.subject-text
	private String subjectText;

	// mail-auth.body-text
	private String bodyText;
}