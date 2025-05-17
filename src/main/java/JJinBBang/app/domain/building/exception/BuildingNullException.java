package JJinBBang.app.domain.building.exception;

import JJinBBang.app.global.error.exception.NotFoundGroupException;

public class BuildingNullException extends NotFoundGroupException {
	public BuildingNullException(String message) {
		super(message);
	}

	public BuildingNullException() {
		super("해당 건물 정보가 존재하지 않습니다.");
	}
}
