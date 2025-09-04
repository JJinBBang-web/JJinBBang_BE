package JJinBBang.app.global.security;

import java.util.HashMap;
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
	/**
	 * 키=HTTP 메서드 (ALL, GET, POST, PUT, PATCH, DELETE)
	 * 값=경로 리스트
	 */
	public static final String METHOD_ALL = "ALL";
	public static final String METHOD_GET = "GET";
	public static final String METHOD_POST = "POST";
	public static final String METHOD_PUT = "PUT";
	public static final String METHOD_PATCH = "PATCH";
	public static final String METHOD_DELETE = "DELETE";


	private Map<String, List<String>> permitAll = new HashMap<>();
	private Map<String, List<String>> authenticated = new HashMap<>();
	private Map<String, List<String>> anonymous = new HashMap<>();
	private Map<String, List<String>> pendingUser = new HashMap<>();
	private Map<String, List<String>> refresh = new HashMap<>();
	/**
	 * 키=VerificationStatus 이름(VERIFIED, EMAIL_VERIFIED, …)
	 * 값=Map<HTTP 메서드, 경로 리스트>
	 */
	private Map<String, Map<String, List<String>>> verificationStatusBased = new HashMap<>();

	private String anyRequest;
}
