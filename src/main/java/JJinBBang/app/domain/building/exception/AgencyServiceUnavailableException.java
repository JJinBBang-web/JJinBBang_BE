package JJinBBang.app.domain.building.exception;


import JJinBBang.app.global.error.exception.ServiceUnavailableGroupException;

public class AgencyServiceUnavailableException extends ServiceUnavailableGroupException {
	public AgencyServiceUnavailableException(String message) {
		super(message);
	}

	public static AgencyServiceUnavailableException getAgencyServiceOverloaded() {
		return new AgencyServiceUnavailableException("공인중개사 조회 작업이 과부화되었습니다. 잠시 후 다시 시도해주세요.");
	}
}
