package JJinBBang.app.domain.building.exception;

import JJinBBang.app.global.error.exception.NotFoundGroupException;

public class ReviewNotFoundException extends NotFoundGroupException {
	public ReviewNotFoundException(String message) {
		super(message);
	}

	public static ReviewNotFoundException missingReview() {
		return new ReviewNotFoundException("해당 리뷰 정보가 존재하지 않습니다.");
	}
}
