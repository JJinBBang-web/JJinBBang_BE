package JJinBBang.app.domain.user.service.login;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "security.oauth.allowed-redirect-uri")
public class RedirectUriProperties {

	private List<String> kakao = new ArrayList<>();
	private List<String> naver = new ArrayList<>();
	private List<String> google = new ArrayList<>();
}
