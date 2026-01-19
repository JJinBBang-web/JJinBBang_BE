package JJinBBang.app.global.sheets.service;

import JJinBBang.app.domain.common.dto.request.ReviewEventRequest;
import JJinBBang.app.global.sheets.dto.UnregisterReasonDto;
import JJinBBang.app.global.sheets.dto.UserOpinionDto;
import JJinBBang.app.global.sheets.enums.OpinionType;

import java.io.IOException;

public interface GoogleSheetsService {

    // 탈퇴 사유
    void appendUnregisterReason(
            UnregisterReasonDto unregisterReasonDto
    ) throws IOException;


    // 문의 내용
    void appendUserOpinion(
            UserOpinionDto userOpinionDto,
            OpinionType opinionType
    ) throws IOException;

    // 리뷰 저장 (지거국 확장 이벤트)
    void appendReviewFromEvent(ReviewEventRequest req);
}
