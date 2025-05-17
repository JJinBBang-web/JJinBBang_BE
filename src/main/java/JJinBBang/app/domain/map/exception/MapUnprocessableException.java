package JJinBBang.app.domain.map.exception;

import JJinBBang.app.global.error.exception.UnprocessableGroupException;

public class MapUnprocessableException extends UnprocessableGroupException {
	public MapUnprocessableException(String message) {
		super(message);
	}

	// 보증금 최소값이 최대값보다 크거나 or 최대값이 최소값보다 작을 경우
	public static MapUnprocessableException invalidDepositRange() {
		return new MapUnprocessableException("보증금 최소값과 최대값을 확인해주세요.");
	}

	// 월세 최소값이 최대값보다 크거나 or 최대값이 최소값보다 작을 경우
	public static MapUnprocessableException invalidMonthlyRentRange() {
		return new MapUnprocessableException("월세 최소값과 최대값을 확인해주세요.");
	}
}
