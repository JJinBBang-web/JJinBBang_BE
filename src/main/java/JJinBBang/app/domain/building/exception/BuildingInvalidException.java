package JJinBBang.app.domain.building.exception;

import JJinBBang.app.global.error.exception.InvalidGroupException;

public class BuildingInvalidException extends InvalidGroupException {
	public BuildingInvalidException(String message) {
		super(message);
	}

	// 공통
	public static BuildingInvalidException requiredBuildingRequest() {
		return new BuildingInvalidException("buildingRequest는 필수입니다.");
	}

	public static BuildingInvalidException requiredBuildingCode() {
		return new BuildingInvalidException("buildingCode는 필수입니다.");
	}

	public static BuildingInvalidException requiredName() {
		return new BuildingInvalidException("name은 필수입니다.");
	}

	public static BuildingInvalidException requiredAddress() {
		return new BuildingInvalidException("address는 필수입니다.");
	}

	public static BuildingInvalidException requiredType() {
		return new BuildingInvalidException("type은 필수입니다.");
	}

	public static BuildingInvalidException requiredLatitude() {
		return new BuildingInvalidException("latitude는 필수입니다.");
	}

	public static BuildingInvalidException requiredLongitude() {
		return new BuildingInvalidException("longitude는 필수입니다.");
	}

	// 타입 규칙
	public static BuildingInvalidException agencyMustBeAgencyType() {
		return new BuildingInvalidException("AGENCY 리뷰는 건물 유형이 AGENCY여야 합니다.");
	}

	public static BuildingInvalidException generalCannotBeAgencyOrDormitory() {
		return new BuildingInvalidException("GENERAL 리뷰는 AGENCY/DORMITORY 유형을 선택할 수 없습니다.");
	}
}
