package JJinBBang.app.global.ocr.dto.response;

public record ClovaField(
        String inferText, // 인식된 텍스트
        double inferConfidence // 인식된 텍스트의 신뢰도 (0~1)
) {
}
