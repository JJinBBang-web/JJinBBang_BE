package JJinBBang.app.global.sheets.dto;

import java.time.LocalDateTime;

// 문의하기 sheets feature
public record UserOpinionDto(
        Long userId,
        Long targetId, // 문의 대상 ID (리뷰 or 건물의 ID)
        String opinion, // 문의(신고) 내용
        LocalDateTime timestamp // 작성 일시
) {
}
