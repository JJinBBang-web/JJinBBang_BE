package JJinBBang.app.global.ocr.exception;

import JJinBBang.app.global.error.exception.InvalidGroupException;

public class OcrInvalidException extends InvalidGroupException {
    public OcrInvalidException(String message) {
        super(message);
    }

    public static OcrInvalidException invalidRequest() {
        return new OcrInvalidException("잘못된 형식의 요청입니다.");
    }
}
