package JJinBBang.app.global.cookie;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CookieUtils {

	private final CookieProperties props;

	@Value("${jwt.expiration-time.refresh-token}")
	private int refreshTtlMilli;


	public void addCookie(HttpServletResponse res, String name, String value, Integer customMaxAgeMilli) {
		int maxAge = customMaxAgeMilli != null ? customMaxAgeMilli : (refreshTtlMilli / 1000);

		Cookie cookie = new Cookie(name, value);
		cookie.setPath("/");
		cookie.setHttpOnly(props.isHttpOnly());
		cookie.setSecure(props.isSecure());
		if (props.getDomain() != null && !props.getDomain().isBlank()) {
			cookie.setDomain(props.getDomain());
		}
		cookie.setMaxAge(maxAge);

		res.addCookie(cookie);

		// SameSite 지원 (Servlet Cookie API엔 없음 → 직접 헤더 추가)
		String header = String.format("%s=%s; Path=/; Max-Age=%d%s%s; SameSite=%s",
			name,
			value,
			maxAge,
			props.isSecure() ? "; Secure" : "",
			props.isHttpOnly() ? "; HttpOnly" : "",
			props.getSameSite()
		);
		if (props.getDomain() != null && !props.getDomain().isBlank()) {
			header += "; Domain=" + props.getDomain();
		}
		res.addHeader("Set-Cookie", header);
	}

	public void deleteCookie(HttpServletResponse res, String name) {
		addCookie(res, name, "", 0);
	}

	/** 객체 → Base64URL (JDK 직렬화) */
	public <T> String serialize(T obj) {
		if (obj == null) return "";
		try (var baos = new ByteArrayOutputStream();
			 var oos  = new ObjectOutputStream(baos)) {
			oos.writeObject(obj);
			oos.flush();
			return java.util.Base64.getUrlEncoder().encodeToString(baos.toByteArray());
		} catch (IOException e) {
			throw new IllegalStateException("Cookie serialize failed", e);
		}
	}

	/** Base64URL → 객체 (JDK 역직렬화) */
	@SuppressWarnings("unchecked")
	public <T> T deserialize(HttpServletRequest req, String name, Class<T> type) {
		if (req.getCookies() == null) return null;
		for (Cookie c : req.getCookies()) {
			if (!name.equals(c.getName())) continue;
			try {
				byte[] bytes = java.util.Base64.getUrlDecoder().decode(c.getValue());
				try (var bais = new ByteArrayInputStream(bytes);
					 var ois  = new ObjectInputStream(bais)) {
					Object obj = ois.readObject();
					return (obj == null) ? null : (T) obj;
				}
			} catch (IOException | ClassNotFoundException | ClassCastException ex) {
				// 실패 시 null 반환 → auth flow에서 "not found" 로 처리
				return null;
			}
		}
		return null;
	}
}
