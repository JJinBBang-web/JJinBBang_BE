package JJinBBang.app.global.ocr.dto.response;

import java.util.List;

public record ClovaOcrResponse(
        String version,
        String requestId,
        long timestamp,
        List<ClovaImageResult> images
) {
}
