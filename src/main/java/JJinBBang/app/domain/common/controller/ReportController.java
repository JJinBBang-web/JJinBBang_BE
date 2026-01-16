package JJinBBang.app.domain.common.controller;

import JJinBBang.app.domain.common.dto.response.ReportInfoResponse;
import JJinBBang.app.domain.common.dto.response.ReportListResponse;
import JJinBBang.app.domain.common.service.ReportService;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.exception.InvalidTokenException;
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
            @AuthenticationPrincipal Users user,
            @RequestParam(name = "category") String category,
            @RequestParam(name = "cursor", required = false) Long cursor,
            @RequestParam(name = "size", required = false) Integer size
    ) {
        ReportListResponse data = reportService.getReportList(
                category != null ?category.toUpperCase() : null,
                cursor,
                (size != null && size > 0) ? size : 10,
                user
        );

        return new ResTemplate<>(HttpStatus.OK, "리포트 목록 조회 성공", data);
    }

    @GetMapping("{reportId}")
    public ResTemplate<ReportInfoResponse> getReportDetail(
            @AuthenticationPrincipal Users user, // nullable
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
        // 콘텐츠 조회 시 인증 필요 x -> 전역 예외처리 적용 시 제거
        if (user == null) throw InvalidTokenException.unauthorized();

        reportService.addLike(user, reportId);
        return new ResTemplate<>(HttpStatus.OK, "리포트 좋아요 추가 성공", null);
    }

    @DeleteMapping("/like/{reportId}")
    public ResTemplate<?> deleteReportLike(
            @AuthenticationPrincipal Users user,
            @PathVariable Long reportId
    ) {
        // 콘텐츠 조회 시 인증 필요 x -> 전역 예외처리 적용 시 제거
        if (user == null) throw InvalidTokenException.unauthorized();

        reportService.deleteLike(user, reportId);
        return new ResTemplate<>(HttpStatus.OK, "리포트 좋아요 삭제 성공", null);
    }

    @PatchMapping("/share/{reportId}")
    public ResTemplate<?> addReportShareCount(
            @PathVariable Long reportId
    ) {
        reportService.addShareCount(reportId);
        return new ResTemplate<>(HttpStatus.OK, "공유수 추가 성공", null);
    }
}
