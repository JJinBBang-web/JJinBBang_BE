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

	// 위도/경도 범위(bounds)가 올바르지 않을 경우 (예: neLat < swLat 또는 neLng < swLng)
	public static MapUnprocessableException invalidGeographicBounds() {
		return new MapUnprocessableException("지도 범위(위도/경도)가 올바르지 않습니다. 북동쪽 좌표가 남서쪽 좌표보다 커야 합니다.");
	}
}
