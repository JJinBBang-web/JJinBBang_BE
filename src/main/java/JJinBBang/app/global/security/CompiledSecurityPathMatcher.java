package JJinBBang.app.global.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import JJinBBang.app.global.common.enums.VerificationStatus;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CompiledSecurityPathMatcher {

	private final SecurityPathProperties props;

	@Getter
	private Map<String, List<RequestMatcher>> permitAllMatchers;
	@Getter
	private Map<String, List<RequestMatcher>> authenticatedMatchers;
	@Getter
	private Map<String, List<RequestMatcher>> anonymousMatchers;
	@Getter
	private Map<String, List<RequestMatcher>> pendingUserMatchers;
	@Getter
	private Map<String, List<RequestMatcher>> refreshMatchers;

	@Getter
	private Map<VerificationStatus, Map<String, List<RequestMatcher>>> verificationMatchers;

	@PostConstruct
	void init() {
		this.permitAllMatchers       = compile(props.getPermitAll());
		this.authenticatedMatchers   = compile(props.getAuthenticated());
		this.anonymousMatchers       = compile(props.getAnonymous());
		this.pendingUserMatchers     = compile(props.getPendingUser());
		this.refreshMatchers         = compile(props.getRefresh());
		this.verificationMatchers    = compileVerification(props.getVerificationStatusBased());
	}

	/** Map<HTTP_METHOD, List<PATTERN>> → Map<HTTP_METHOD, List<RequestMatcher>> */
	private Map<String, List<RequestMatcher>> compile(Map<String, List<String>> raw) {
		if (raw == null) return Map.of();
		Map<String, List<RequestMatcher>> out = new HashMap<>();
		for (Map.Entry<String, List<String>> e : raw.entrySet()) {
			String methodKey = e.getKey(); // "ALL" | "GET" | ...
			List<RequestMatcher> compiled = e.getValue() == null ? List.of() :
				e.getValue().stream()
					.filter(Objects::nonNull)
					.map(String::trim)
					.filter(s -> !s.isEmpty())
					.map(p -> toMatcher(methodKey, p))
					.toList();
			out.put(methodKey, compiled);
		}
		return Collections.unmodifiableMap(out);
	}

	/** Map<Status, Map<HTTP_METHOD, List<PATTERN>>> → Map<Status, Map<HTTP_METHOD, List<RequestMatcher>>> */
	private Map<VerificationStatus, Map<String, List<RequestMatcher>>> compileVerification(
		Map<String, Map<String, List<String>>> raw) {
		if (raw == null) return Map.of();
		Map<VerificationStatus, Map<String, List<RequestMatcher>>> out = new EnumMap<>(VerificationStatus.class);
		for (Map.Entry<String, Map<String, List<String>>> e : raw.entrySet()) {
			VerificationStatus status = VerificationStatus.valueOf(e.getKey());
			out.put(status, compile(e.getValue()));
		}
		return Collections.unmodifiableMap(out);
	}

	/** HTTP 메서드 키(+패턴) → AntPathRequestMatcher 사전 컴파일 */
	private RequestMatcher toMatcher(String methodKey, String pattern) {
		String httpMethod = SecurityPathProperties.METHOD_ALL.equalsIgnoreCase(methodKey) ? null : methodKey;
		// httpMethod가 null이면 모든 메서드 허용
		return new AntPathRequestMatcher(pattern, httpMethod);
	}

	/** ALL + 특정 메서드의 매처들을 합쳐서 불변 리스트로 반환 */
	public List<RequestMatcher> mergeByMethod(Map<String, List<RequestMatcher>> compiled, String method) {
		if (compiled == null) return List.of();
		List<RequestMatcher> list = new ArrayList<>();
		list.addAll(compiled.getOrDefault(SecurityPathProperties.METHOD_ALL, List.of()));
		list.addAll(compiled.getOrDefault(method, List.of()));
		return List.copyOf(list);
	}
}
