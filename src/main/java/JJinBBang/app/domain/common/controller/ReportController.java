package JJinBBang.app.domain.common.controller;

import JJinBBang.app.domain.common.dto.response.ReportInfoResponse;
import JJinBBang.app.domain.common.dto.response.ReportListResponse;
import JJinBBang.app.domain.common.service.ReportService;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/report")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("")
    public ResTemplate<ReportListResponse> getReportList(
            @RequestParam(name = "category") String category,
            @RequestParam(name = "cursor", required = false) Integer cursor,
            @RequestParam(name = "size", required = false) Integer size
    ) {
        int currentCursor = (cursor != null) ? cursor : 0;
        int pageSize = (size != null) ? size : 10;

        ReportListResponse data = reportService.getReportList(category.toUpperCase(), currentCursor, pageSize);

        return new ResTemplate<>(HttpStatus.OK, "리포트 목록 조회 성공", data);
    }

    @GetMapping("{reportId}")
    public ResTemplate<ReportInfoResponse> getReportDetail(
            @AuthenticationPrincipal Users user,
            @PathVariable Long reportId
    ) {
        ReportInfoResponse data = reportService.getReportDetail(user, reportId);
        return new ResTemplate<>(HttpStatus.OK, "리포트 조회 성공", data);
    }

    @PostMapping("/like/{reportId}")
    public ResTemplate<?> addReportLike(
            @AuthenticationPrincipal Users user,
            @PathVariable Long reportId
    ) {
        return new ResTemplate<>(HttpStatus.OK, "리포트 좋아요 추가 성공", null);
    }

    @DeleteMapping("/like/{reportId}")
    public ResTemplate<?> deleteReportLike(
            @AuthenticationPrincipal Users user,
            @PathVariable Long reportId
    ) {
        return new ResTemplate<>(HttpStatus.OK, "리포트 좋아요 삭제 성공", null);
    }
}
