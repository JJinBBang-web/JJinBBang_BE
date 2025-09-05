package JJinBBang.app.global.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import JJinBBang.app.global.common.enums.VerificationStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityPathMatchUtil {

	private final SecurityPathProperties securityPathProperties;
	private final CompiledSecurityPathMatcher compiled;

	/** 약관 동의(회원가입 진행 중) 경로 매칭 */
	public boolean pendingUserMatch(HttpServletRequest request) {
		return match(request, compiled.getPendingUserMatchers());
	}

	/** 리프레시 토큰 전용 경로 매칭 */
	public boolean refreshTokenMatch(HttpServletRequest request) {
		return match(request, compiled.getRefreshMatchers());
	}

	/** 검증 상태별 경로 매칭 — 일치하는 requiredStatus가 있으면 true 반환 */
	public VerificationStatus matchAnyVerificationRequired(HttpServletRequest request) {
		String method = request.getMethod();
		for (Map.Entry<VerificationStatus, Map<String, List<RequestMatcher>>> e
			: compiled.getVerificationMatchers().entrySet()) {
			List<RequestMatcher> merged = compiled.mergeByMethod(e.getValue(), method);
			if (merged.stream().anyMatch(m -> m.matches(request))) {
				return e.getKey();
			}
		}
		return null; // 일치 없음
	}

	private boolean match(HttpServletRequest request, Map<String, List<RequestMatcher>> table) {
		String method = request.getMethod();
		List<RequestMatcher> merged = compiled.mergeByMethod(table, method);
		for (RequestMatcher m : merged) {
			if (m.matches(request)) return true;
		}
		return false;
	}
}
