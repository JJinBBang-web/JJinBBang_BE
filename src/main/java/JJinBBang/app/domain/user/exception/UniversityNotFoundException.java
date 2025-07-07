package JJinBBang.app.domain.user.exception;

import JJinBBang.app.global.error.exception.NotFoundGroupException;

public class UniversityNotFoundException extends NotFoundGroupException {

    public UniversityNotFoundException() {
        super("대학교 데이터 없음");
    }

}
