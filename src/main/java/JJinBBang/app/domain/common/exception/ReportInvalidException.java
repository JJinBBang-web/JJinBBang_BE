package JJinBBang.app.domain.common.exception;

import JJinBBang.app.global.error.exception.InvalidGroupException;

public class ReportInvalidException extends InvalidGroupException {
    public ReportInvalidException(String message) {
        super(message);
    }

    public static ReportInvalidException invalidCategory() {return new ReportInvalidException("잘못된 카테고리 입니다.");}

    public static ReportInvalidException invalidCursor() {return new ReportInvalidException("cursor는 항상 0 이상입니다.");}

    public static ReportInvalidException invalidSize() {return new ReportInvalidException("size는 항상 0 이상입니다.");}
}
