package JJinBBang.app.global.security.redirect;

import static JJinBBang.app.global.cookie.CookieType.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.stereotype.Component;

import JJinBBang.app.global.cookie.CookieUtils;
import JJinBBang.app.global.security.exception.SecurityAuthException;
import JJinBBang.app.global.security.properties.WhitelistDomain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuthRedirectCookieManager {

	private final CookieUtils cookieUtils;
	private final WhitelistDomain whitelistDomain;

	public String resolveAndStore(HttpServletRequest req, HttpServletResponse res) {
		String encoded = req.getParameter("redirect");
		if (encoded == null || encoded.isBlank()) {
			throw SecurityAuthException.notAllowedOAuthRedirectUri();
		}

		String decoded = decode(encoded);
		validateWhitelist(decoded);

		String normalized = Base64.getUrlEncoder().encodeToString(decoded.getBytes(StandardCharsets.UTF_8));
		cookieUtils.addCookie(res, REDIRECT_URI_PARAM_COOKIE, normalized, 300);
		return decoded;
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
