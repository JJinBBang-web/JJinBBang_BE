package JJinBBang.app.global.security.exception;

import JJinBBang.app.global.error.exception.AuthGroupException;
import org.springframework.security.core.AuthenticationException;

public class SecurityAuthException extends AuthenticationException {
	public SecurityAuthException(String message) {
		super(message);
	}

	public static SecurityAuthException noPermission() {
		return new SecurityAuthException("해당 요청에 대한 권한이 없습니다.");
	}

	public static SecurityAuthException noAuthentication() {
		return new SecurityAuthException("인증되지 않은 사용자입니다. 로그인 후 다시 시도하세요.");
	}
}
