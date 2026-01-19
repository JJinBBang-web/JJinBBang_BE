package JJinBBang.app.global.sheets;

import JJinBBang.app.global.ocr.exception.OcrInternalException;

public class GoogleInternalException extends RuntimeException {
    public GoogleInternalException(String message) {
        super(message);
    }

    public static OcrInternalException apiError() {
        return new OcrInternalException("Google Sheets 업로드 중 오류가 발생했습니다.");
    }
}
