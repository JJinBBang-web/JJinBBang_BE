package JJinBBang.app.global.security.service;

import static JJinBBang.app.global.cookie.CookieType.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import JJinBBang.app.global.cookie.CookieUtils;
import JJinBBang.app.global.security.exception.SecurityAuthException;
import JJinBBang.app.global.security.properties.WhitelistDomain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuthRedirectCookieService {

	private final CookieUtils cookieUtils;
	private final WhitelistDomain whitelistDomain;

	@Value("${jwt.expiration-time.redirect-uri-param}")
	private long redirectUriParamTtlMillis;

	public String resolveAndStore(HttpServletRequest req, HttpServletResponse res) {
		String encoded = req.getParameter("redirect");
		if (encoded == null || encoded.isBlank()) {
			throw SecurityAuthException.notAllowedOAuthRedirectUri();
		}

		String decoded = decode(encoded);
		validateWhitelist(decoded);

		String normalized = Base64.getUrlEncoder().encodeToString(decoded.getBytes(StandardCharsets.UTF_8));
		cookieUtils.addCookie(res, REDIRECT_URI_PARAM_COOKIE, normalized, redirectUriParamTtlMillis);
		return decoded;
	}

	public String resolveFromCookie(HttpServletRequest req) {
		Cookie cookie = findCookie(req, REDIRECT_URI_PARAM_COOKIE);
		if (cookie == null) {
			throw SecurityAuthException.notAllowedOAuthRedirectUri();
		}
		try {
			return new String(Base64.getUrlDecoder().decode(cookie.getValue()), StandardCharsets.UTF_8);
		} catch (IllegalArgumentException ex) {
			throw SecurityAuthException.notAllowedOAuthRedirectUri();
		}
	}

	private static Cookie findCookie(HttpServletRequest req, String name) {
		if (req.getCookies() == null) {
			return null;
		}
		for (Cookie cookie : req.getCookies()) {
			if (name.equals(cookie.getName())) {
				return cookie;
			}
		}
		return null;
	}

	private static String decode(String encoded) {
		try {
			return new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
		} catch (IllegalArgumentException e) {
			throw SecurityAuthException.notAllowedOAuthRedirectUri();
		}
	}

	private void validateWhitelist(String redirect) {
		for (String prefix : whitelistDomain.getDomains()) {
			if (redirect.startsWith(prefix)) {
				return;
			}
		}
		throw SecurityAuthException.notAllowedOAuthRedirectUri();
	}
}
