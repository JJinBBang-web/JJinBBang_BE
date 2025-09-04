package JJinBBang.app.global.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityPathMatcher {

	private final SecurityPathProperties securityPathProperties;
	private final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

	// 약관 동의 중인 유저 관련 API 매칭
	public boolean pendingUserMatch(HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		String method = request.getMethod();
		System.out.println("request = " + requestURI + ", method = " + method);
		return pendingUserMatch(requestURI, method);
	}
	public boolean pendingUserMatch(String requestURI, String method) {
		Map<String, List<String>> pendingUserApi = securityPathProperties.getPendingUser();
		return match(requestURI, method, pendingUserApi);
	}

	// 엑세스 토큰 재발급 관련 API 매칭
	public boolean refreshTokenMatch(HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		String method = request.getMethod();
		System.out.println("request = " + requestURI + ", method = " + method);
		return refreshTokenMatch(requestURI, method);
	}
	public boolean refreshTokenMatch(String requestURI, String method) {
		Map<String, List<String>> refreshTokenApi = securityPathProperties.getRefresh();
		return match(requestURI, method, refreshTokenApi);
	}


	// 공통 매칭 메서드
	public boolean match(HttpServletRequest request, Map<String, List<String>> pathMap) {
		String requestURI = request.getRequestURI();
		String method = request.getMethod();
		System.out.println("request = " + requestURI + ", method = " + method);
		return match(requestURI, method, pathMap);
	}

	public boolean match(String requestURI, String method, Map<String, List<String>> pathMap) {
		List<String> paths = new ArrayList<>();
		paths.addAll(pathMap.getOrDefault(SecurityPathProperties.METHOD_ALL, List.of())); // ALL 메서드에 해당하는 경로 추가
		paths.addAll(pathMap.getOrDefault(method, List.of())); // 요청 메서드에 해당하는 경로 추가

		return paths.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, requestURI));
	}
}
