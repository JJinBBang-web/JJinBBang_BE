package JJinBBang.app.domain.user.dto.request;

import JJinBBang.app.global.sheets.enums.OpinionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 문의하기(신고하기) - 신고내용
public record UserOpinionRequest(
        @NotBlank
        String opinion,

        @NotNull
        OpinionType opinionType, // 문의 타입

        Long targetId // 신고 대상 (리뷰, 빌딩) - null의 경우 일반 문의
) {
}
