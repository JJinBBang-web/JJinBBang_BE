package JJinBBang.app.global.ocr.dto.request;

public record ClovaImage(
        String format, // 이미지 형식
        String name, // 이미지 이름
        String url // 이미지 URL (직접 이미지를 불러올 경우 Base64 인코딩 된 data 사용)
) {
}
