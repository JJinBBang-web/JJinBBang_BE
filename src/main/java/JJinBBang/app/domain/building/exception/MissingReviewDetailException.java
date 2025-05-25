package JJinBBang.app.domain.building.exception;

import JJinBBang.app.global.error.exception.InternalServerErrorGroupException;

public class MissingReviewDetailException extends InternalServerErrorGroupException {
	public MissingReviewDetailException(String message) {
		super(message);
	}

	public MissingReviewDetailException(Long reviewId) {
		super("후기(id=" + reviewId + ") 상세 정복 생성되지 않았습니다.");
	}
}
