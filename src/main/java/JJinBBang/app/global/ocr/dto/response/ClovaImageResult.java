package JJinBBang.app.global.ocr.dto.response;

import java.util.List;

public record ClovaImageResult(
        String uid,
        String name,
        String inferResult, // 이미지 인식 결과 (SUCCESS | FAILURE | ERROR)
        List<ClovaField> fields
) {
}
