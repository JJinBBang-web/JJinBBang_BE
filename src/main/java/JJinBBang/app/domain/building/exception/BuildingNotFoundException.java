package JJinBBang.app.domain.building.exception;

import JJinBBang.app.global.error.exception.NotFoundGroupException;

public class BuildingNotFoundException extends NotFoundGroupException {
	public BuildingNotFoundException(String message) {
		super(message);
	}

	public static BuildingNotFoundException unsupportedDormitoryFacility() {
		return new BuildingNotFoundException("존재하지 않는 기숙사 시설입니다");
	}

	public static BuildingNotFoundException missingDormitory() {
		return new BuildingNotFoundException("기숙사 정보가 존재하지 않습니다");
	}
}