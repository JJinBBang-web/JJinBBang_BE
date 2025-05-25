package JJinBBang.app.domain.building.exception;

import JJinBBang.app.global.error.exception.InternalServerErrorGroupException;

public class ReviewInternalException extends InternalServerErrorGroupException {
	public ReviewInternalException(String message) {
		super(message);
	}

	public ReviewInternalException() {
		super("잘못된 리뷰 유형입니다.");
	}
}
