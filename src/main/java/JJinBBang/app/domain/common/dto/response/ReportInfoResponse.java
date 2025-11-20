package JJinBBang.app.domain.common.dto.response;

import JJinBBang.app.domain.common.enums.ReportCategory;

import java.time.LocalDateTime;

public record ReportInfoResponse(
        Long id,
        ReportCategory category,
        String title,
        String content,
        LocalDateTime createdAt,
        Integer likeCount,
        Integer viewCount,
        Integer shareCount,
        boolean isLiked
) {
}
