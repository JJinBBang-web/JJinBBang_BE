package JJinBBang.app.domain.building.exception;

public class AgencyInternalServerErrorException extends RuntimeException {
	public AgencyInternalServerErrorException(String message) {
		super(message);
	}

	public static AgencyInternalServerErrorException getAgencyInternalServerError() {
		return new AgencyInternalServerErrorException("공인중개사 조회 작업 중 내부 서버 오류가 발생했습니다.");
	}
}
