package JJinBBang.app.domain.map.exception;

import JJinBBang.app.global.error.exception.NoContentGroupException;

public class MapNoContentException extends NoContentGroupException {
	public MapNoContentException(String message) {
		super(message);
	}

	public static MapNoContentException notFoundReview() {
		return new MapNoContentException("리뷰가 존재하지 않습니다.");
	}

	public static MapNoContentException notFoundBuilding() {
		return new MapNoContentException("건물이 존재하지 않습니다.");
	}

	public static MapNoContentException searchFailed() {
		return new MapNoContentException("검색 결과가 존재하지 않습니다.");
	}
}
