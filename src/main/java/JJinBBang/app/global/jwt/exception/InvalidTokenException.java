package JJinBBang.app.global.jwt.exception;

import JJinBBang.app.global.error.exception.AuthGroupException;

public class InvalidTokenException extends AuthGroupException {
	public InvalidTokenException(String message) {
		super(message);
	}

	public static InvalidTokenException expired(){
		return new InvalidTokenException("JWT 토큰이 만료되었습니다.");
	}

	public static InvalidTokenException invalidToken(){
		return new InvalidTokenException("유효하지 않은 JWT 토큰입니다.");
	}

	public static InvalidTokenException signupTokenNotAllowed(){
		return new InvalidTokenException("회원가입 토큰은 회원가입 시에만 사용할 수 있습니다.");
	}

	public static InvalidTokenException authTokenNotAllowed(){
		return new InvalidTokenException("인증 토큰은 회원가입 시에 사용할 수 없습니다.");
	}
}
