package JJinBBang.app.global.security;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "security.path")
public class SecurityPathProperties {
	private List<String> permitAll;
	private List<String> authenticated;
	private List<String> anonymous;
	private Map<String, List<String>> verificationStatusBased;
	private List<String> pendingUser;
	private List<String> refresh;
	private String anyRequest;
}
