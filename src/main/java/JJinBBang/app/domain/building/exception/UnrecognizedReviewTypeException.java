package JJinBBang.app.domain.building.exception;

import JJinBBang.app.global.error.exception.InternalServerErrorGroupException;

public class UnrecognizedReviewTypeException extends InternalServerErrorGroupException {

	public UnrecognizedReviewTypeException(String message) {
		super(message);
	}

	public UnrecognizedReviewTypeException() {
		super("지원되지 않는 리뷰 타입입니다");
	}
}
