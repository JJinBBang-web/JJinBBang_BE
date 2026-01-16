package JJinBBang.app.global.ocr.dto.response;

public record OcrResult(
        String text,
        double confidence
) {
}
