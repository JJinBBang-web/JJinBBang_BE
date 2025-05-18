package JJinBBang.app.domain.building.exception;

import JJinBBang.app.global.error.exception.InternalServerErrorGroupException;

public class ReviewDetailInternalException extends InternalServerErrorGroupException {
	public ReviewDetailInternalException(String message) {
		super(message);
	}

	public ReviewDetailInternalException(long reviewId) {
		super("등록된 리뷰 상세 정보가 없습니다. reviewId=" + reviewId);
	}
}
