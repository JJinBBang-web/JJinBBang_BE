package JJinBBang.app.global.ocr.dto.request;

import java.util.List;

// Google Drive에 저장된 이미지를 불러오기 때문에 아래 포맷으로 진행
public record ClovaOcrRequest(
        String version,
        String requestId,
        long timestamp,
        List<ClovaImage> images
) {
}
