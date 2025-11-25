package JJinBBang.app.domain.common.exception;

import JJinBBang.app.global.error.exception.NotFoundGroupException;

public class ReportNotFoundGroupException extends NotFoundGroupException {
    public ReportNotFoundGroupException(String message) {
        super(message);
    }

    public static ReportNotFoundGroupException reportNotFound() {return new ReportNotFoundGroupException("해당 리포트를 찾을 수 없습니다.");}
}
