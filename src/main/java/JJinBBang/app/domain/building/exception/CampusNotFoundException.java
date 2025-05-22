package JJinBBang.app.domain.building.exception;

import JJinBBang.app.global.error.exception.NotFoundGroupException;

public class CampusNotFoundException extends NotFoundGroupException {
    public CampusNotFoundException(String message) {
        super(message);
    }

    public static CampusNotFoundException missingCampus() {
        return new CampusNotFoundException("해당 캠퍼스 정보가 존재하지 않습니다.");
    }
}
