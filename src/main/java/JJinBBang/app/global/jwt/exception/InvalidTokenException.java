package JJinBBang.app.global.jwt.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidTokenException extends AuthenticationException {
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

	public static InvalidTokenException accessTokenNotAllowed(){
		return new InvalidTokenException("엑세스 토큰을 사용할 수 없는 요청입니다.");
	}
	public static InvalidTokenException refreshTokenNotAllowed(){
		return new InvalidTokenException("리프레시 토큰을 사용할 수 없는 요청입니다.");
	}

	public static InvalidTokenException deletedUser() {
		return new InvalidTokenException("탈퇴한 유저입니다.");
	}

	public static InvalidTokenException userNotFound() {
		return new InvalidTokenException("존재하지 않는 유저입니다.");
	}
}
