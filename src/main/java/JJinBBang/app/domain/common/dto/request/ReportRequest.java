package JJinBBang.app.domain.common.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReportRequest(
        @NotBlank String category,
        @NotBlank String coverImage,
        @NotBlank String title,
        @NotBlank String content
) {
}
