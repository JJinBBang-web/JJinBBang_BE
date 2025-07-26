package JJinBBang.app.domain.user.exception;

import JJinBBang.app.global.error.exception.NotFoundGroupException;

public class UniversityNotFoundException extends NotFoundGroupException {

    public UniversityNotFoundException() {
        super("대학교 데이터 없음");
    }
    public UniversityNotFoundException(String message) {
        super(message);
    }

    public static UniversityNotFoundException universityNotFound(String universityName) {
        return new UniversityNotFoundException("해당 도메인에 대한 대학교 데이터 없음: " + universityName);
    }

}
