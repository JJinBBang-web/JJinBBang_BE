package JJinBBang.app.domain.user.dto.request;

import JJinBBang.app.global.sheets.enums.OpinionType;

// 문의하기(신고하기) - 신고내용
public record UserOpinionRequest(
        String opinion,
        OpinionType opinionType, // 문의 타입
        Long targetId // 신고 대상 (리뷰, 빌딩)
) {
}
