package JJinBBang.app.global.sheets.dto;

import java.time.LocalDateTime;

// 문의하기 sheets feature
public record UserOpinionDto(
        Long userId,
        String opinion, // 문의(신고) 내용
        LocalDateTime timestamp // 작성 일시
) {
}
