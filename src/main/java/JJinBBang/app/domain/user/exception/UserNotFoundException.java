package JJinBBang.app.domain.user.exception;

import JJinBBang.app.global.error.exception.NotFoundGroupException;

public class UserNotFoundException extends NotFoundGroupException {
	public UserNotFoundException(String message) {
		super(message);
	}

	public static UserNotFoundException notFound(){
		return new UserNotFoundException("찾으려는 유저가 DB에 존재하지 않습니다.");
	}

	public static UserNotFoundException searchFailed(){
		return new UserNotFoundException("유저 조회에 실패하였습니다.");
	}

	public static UserNotFoundException systemError() {
		return new UserNotFoundException("시스템 에러입니다.");
	}
}
