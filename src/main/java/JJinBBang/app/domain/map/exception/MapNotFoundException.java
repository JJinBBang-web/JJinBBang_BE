package JJinBBang.app.domain.map.exception;

import JJinBBang.app.global.error.exception.NotFoundGroupException;

public class MapNotFoundException extends NotFoundGroupException {
	public MapNotFoundException(String message) {
		super(message);
	}

	public static MapNotFoundException notFoundBuilding() {
		return new MapNotFoundException("해당 건물이 존재하지 않습니다.");
	}

	public static MapNotFoundException notFoundReview() {
		return new MapNotFoundException("해당 리뷰가 존재하지 않습니다.");
	}

	public static MapNotFoundException notFoundAgency() {
		return new MapNotFoundException("해당 공인중개사가 존재하지 않습니다.");
	}
}
