package JJinBBang.app.global.sheets.service;

import JJinBBang.app.global.sheets.dto.UnregisterReasonDto;
import JJinBBang.app.global.sheets.dto.UserOpinionDto;

import java.io.IOException;

public interface GoogleSheetsService {

    // 탈퇴 사유
    void appendUnregisterReason(
            UnregisterReasonDto unregisterReasonDto
    ) throws IOException;

    // 문의 내용
    void appendUserOpinion(
            UserOpinionDto userOpinionDto
    ) throws IOException;
}
