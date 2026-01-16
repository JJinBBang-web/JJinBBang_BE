package JJinBBang.app.global.common.dto;

import JJinBBang.app.domain.common.enums.ReportCategory;

import java.time.LocalDateTime;

public record ReportInfo(
        Long id,
        String coverImage, // 대표 이미지
        ReportCategory category, // 리포트 분류
        String title, // 리포트 제목
        LocalDateTime createdAt, // 작성일
        Integer likeCount, // 좋아요 수
        Integer viewCount, // 조회 수
        boolean isLiked // 좋아요 여부
) {
}
