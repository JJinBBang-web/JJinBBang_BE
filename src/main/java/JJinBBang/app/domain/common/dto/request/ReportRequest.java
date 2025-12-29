package JJinBBang.app.domain.common.dto.request;

public record ReportRequest(
        String category,
        String coverImage,
        String title,
        String content
) {
}
