package JJinBBang.app.global.ocr.exception;

import JJinBBang.app.global.error.exception.InternalServerErrorGroupException;

public class OcrInternalException extends InternalServerErrorGroupException {
    public OcrInternalException(String message) {
        super(message);
    }

    public static OcrInternalException apiError() {
        return new OcrInternalException("OCR API 연동 과정에서 오류가 발생했습니다.");
    }

    public static OcrInternalException ocrFail() {
        return new OcrInternalException("OCR 처리 과정에서 오류가 발생했습니다.");
    }
}
