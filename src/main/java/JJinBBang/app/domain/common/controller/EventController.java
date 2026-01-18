package JJinBBang.app.domain.common.controller;

import JJinBBang.app.domain.common.dto.request.ReviewEventRequest;
import JJinBBang.app.global.sheets.service.GoogleSheetsService;
import JJinBBang.app.global.template.ResTemplate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/event")
@RequiredArgsConstructor
public class EventController {

    private final GoogleSheetsService googleSheetsService;

    /**
     * 리뷰 이벤트 리뷰 수집 API - 지거국 10개 대학 확장 이벤트
     * Google Sheets 저장
     *
     * @param req
     * @return
     * @throws IOException
     */
    @PostMapping("/review")
    public ResTemplate<?> addReviewFromEvent(
            @Valid @RequestBody ReviewEventRequest req
    ) {
        googleSheetsService.appendReviewFromEvent(req);
        return new ResTemplate<>(HttpStatus.OK, "리뷰이벤트 후기 업로드 성공", null);
    }
}
