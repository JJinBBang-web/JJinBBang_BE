package JJinBBang.app.global.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.security.exception.SecurityAuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	private final SecurityPathProperties securityPathProperties;
	private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
		AccessDeniedException accessDeniedException) throws IOException {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		String userVerificationStatus = "UNKNOWN"; // 기본값 설정

		if (authentication != null && authentication.getPrincipal() instanceof Users user) {
			userVerificationStatus = user.getVerificationStatus().name();
		}

		String requestURI = request.getRequestURI();
		String requiredVerificationStatus = getRequiredVerificationStatus(requestURI);
		String message = getCustomErrorMessage(userVerificationStatus, requiredVerificationStatus);

		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType("application/json;charset=UTF-8");

		Map<String, Object> errorDetails = new HashMap<>();
		errorDetails.put("code", HttpServletResponse.SC_FORBIDDEN);
		errorDetails.put("message", message);

		// 로깅 필요 시 log.error 으로 변경 후 아래 정보 출력?
		// String username = (authentication != null) ? authentication.getName() : "anonymous";
		// errorDetails.put("user", username);
		// errorDetails.put("path", requestURI);
		// errorDetails.put("verificationStatus", userVerificationStatus);
		// errorDetails.put("requiredVerificationStatus", requiredVerificationStatus);

		ObjectMapper mapper = new ObjectMapper();
		response.getWriter().write(mapper.writeValueAsString(errorDetails));
	}

	/**
	 * 요청된 URL이 요구하는 verificationStatus 가져오기
	 */
	private String getRequiredVerificationStatus(String requestURI) {
		for (Map.Entry<String, List<String>> entry : securityPathProperties.getVerificationStatusBased().entrySet()) {
			for (String pattern : entry.getValue()) {
				if (PATH_MATCHER.match(pattern, requestURI)) {  // ✅ AntPathMatcher 사용하여 와일드카드 패턴 매칭
					return entry.getKey();
				}
			}
		}
		return "UNKNOWN";
	}

	/**
	 * 현재 verificationStatus와 필요한 verificationStatus를 비교하여 예외 메시지 생성
	 */
	private String getCustomErrorMessage(String userStatus, String requiredStatus) {
		if (!userStatus.equals(requiredStatus)) {
			switch (requiredStatus) {
				case "VERIFIED":
					return SecurityAuthException.universityVerificationRequired().getMessage();
				case "UNVERIFIED":
					if ("VERIFIED".equals(userStatus)) {
						return SecurityAuthException.alreadyUniversityVerified().getMessage();
					} else if ("PENDING".equals(userStatus)) {
						return SecurityAuthException.existPendingUnivVerifyRequest().getMessage();
					}
				case "PENDING":
					if ("VERIFIED".equals(userStatus)) {
						return SecurityAuthException.alreadyUniversityVerified().getMessage();
					} else if ("UNVERIFIED".equals(userStatus)) {
						return SecurityAuthException.pendingUnivVerifyRequestNotFound().getMessage();
					}
			}
		}
		return SecurityAuthException.noPermission().getMessage();
	}
}
