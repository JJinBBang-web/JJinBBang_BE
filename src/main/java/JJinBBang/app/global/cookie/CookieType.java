package JJinBBang.app.global.cookie;

import lombok.Getter;

@Getter
public final class CookieType {
	CookieType() {}

	public static final String REFRESH_TOKEN_COOKIE = "REFRESH_TOKEN";
	public static final String PENDING_TOKEN_COOKIE = "PENDING_TOKEN";
	public static final String OAUTH2_AUTH_REQUEST_COOKIE = "OAUTH2_AUTH_REQ";
	public static final String REDIRECT_URI_PARAM_COOKIE = "REDIRECT_URI";
}
