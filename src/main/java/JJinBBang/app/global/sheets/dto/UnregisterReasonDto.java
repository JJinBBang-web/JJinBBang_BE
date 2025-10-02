package JJinBBang.app.global.sheets.dto;

import java.time.LocalDateTime;

// 탈퇴 사유 조회 sheets feature
public record UnregisterReasonDto(
        Long userId,
        Integer option, // 선택 번호
        String unregisterReason, // 탈퇴 사유
        LocalDateTime registeredAt, // 가입일
        LocalDateTime unregisteredAt // 탈퇴일
) {
}
