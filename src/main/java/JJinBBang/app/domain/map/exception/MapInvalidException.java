package JJinBBang.app.domain.map.exception;

import JJinBBang.app.global.error.exception.InvalidGroupException;

public class MapInvalidException extends InvalidGroupException {
	public MapInvalidException(String message) {
		super(message);
	}

	public static MapInvalidException invalidKeyword() {
		return new MapInvalidException("키워드가 유효하지 않습니다.");
	}

	public static MapInvalidException invalidTags() {
		return new MapInvalidException("태그가 유효하지 않습니다.");
	}

	public static MapInvalidException invalidUniv() {
		return new MapInvalidException("해당 대학이 존재하지 않거나 일치하지 않습니다.");
	}
}
