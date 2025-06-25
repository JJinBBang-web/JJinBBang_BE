package JJinBBang.app.domain.building.exception;

import JJinBBang.app.global.error.exception.InternalServerErrorGroupException;

public class ReviewInternalServerErrorException extends InternalServerErrorGroupException {
	public ReviewInternalServerErrorException(String message) {
		super(message);
	}

	public static ReviewInternalServerErrorException notSupportReviewType() {
		return new ReviewInternalServerErrorException("지원되지 않는 리뷰 타입입니다.");
	}
}