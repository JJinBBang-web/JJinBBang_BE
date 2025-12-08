package JJinBBang.app.global.cookie;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CookieUtils {

	private final CookieProperties props;
	private final ObjectMapper objectMapper;

	/** Refresh Token 쿠키 기본 만료 시간 (밀리초) */
	@Value("${jwt.expiration-time.refresh-token}")
	private long refreshTtlMillis;

	public void addCookie(HttpServletResponse res, String name, String value, Long customMaxAgeMillis) {
		// 밀리초를 초로 변환 (Cookie.setMaxAge()는 초 단위)
		long maxAgeMillis = customMaxAgeMillis != null ? customMaxAgeMillis : refreshTtlMillis;
		int maxAgeSeconds = (int) (maxAgeMillis / 1000L); // long으로 나눈 후 int로 캐스팅하여 오버플로우 방지

		Cookie cookie = new Cookie(name, value);
		cookie.setPath("/");
		cookie.setHttpOnly(props.isHttpOnly());
		cookie.setSecure(props.isSecure());
		if (props.getDomain() != null && !props.getDomain().isBlank()) {
			cookie.setDomain(props.getDomain());
		}
		cookie.setMaxAge(maxAgeSeconds);

		// SameSite 지원 (Servlet Cookie API엔 없음 → 직접 헤더 추가)
		String header = String.format("%s=%s; Path=/; Max-Age=%d%s%s; SameSite=%s",
				name,
				value,
				maxAgeSeconds,
				props.isSecure() ? "; Secure" : "",
				props.isHttpOnly() ? "; HttpOnly" : "",
				props.getSameSite());
		if (props.getDomain() != null && !props.getDomain().isBlank()) {
			header += "; Domain=" + props.getDomain();
		}
		res.addHeader("Set-Cookie", header);
	}

	public void deleteCookie(HttpServletResponse res, String name) {
		addCookie(res, name, "", 0L);
	}

	/** 객체 → Base64URL (JSON 직렬화) */
	public <T> String serialize(T obj) {
		if (obj == null)
			return "";
		try {
			String json = objectMapper.writeValueAsString(obj);
			return java.util.Base64.getUrlEncoder()
					.encodeToString(json.getBytes(StandardCharsets.UTF_8));
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Cookie serialize failed", e);
		}
	}

	/** Base64URL → 객체 (JSON 역직렬화) */
	public <T> T deserialize(HttpServletRequest req, String name, Class<T> type) {
		if (req.getCookies() == null)
			return null;
		for (Cookie c : req.getCookies()) {
			if (!name.equals(c.getName()))
				continue;
			try {
				byte[] bytes = java.util.Base64.getUrlDecoder().decode(c.getValue());
				String json = new String(bytes, StandardCharsets.UTF_8);
				return objectMapper.readValue(json, type);
			} catch (IOException | IllegalArgumentException ex) {
				// 실패 시 null 반환 → auth flow에서 "not found" 로 처리
				return null;
			}
		}
		return null;
	}
}
