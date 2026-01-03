package JJinBBang.app.domain.common.controller;

import JJinBBang.app.domain.common.dto.request.ReportRequest;
import JJinBBang.app.domain.common.exception.AdminAccessException;
import JJinBBang.app.domain.common.service.ReportAdminService;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.enums.UserRole;
import JJinBBang.app.domain.user.exception.InvalidTokenException;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/report")
public class ReportAdminController {

    private final ReportAdminService reportAdminService;

    @PostMapping("")
    public ResTemplate<?> createReport(
            @AuthenticationPrincipal Users user,
            @RequestBody ReportRequest req
    ) {
        if (user == null) throw InvalidTokenException.unauthorized();

        // 일반 사용자 접근 시 예외처리
        if (user.getRole() != UserRole.ADMIN) throw AdminAccessException.accessDenied();

        reportAdminService.createReport(req);
        return new ResTemplate<>(HttpStatus.CREATED, "콘텐츠 작성 성공", null);
    }

    @PutMapping("{reportId}")
    public ResTemplate<?> updateReport(
            @PathVariable Long reportId,
            @AuthenticationPrincipal Users user,
            @RequestBody ReportRequest req
    ) {
        if (user == null) throw InvalidTokenException.unauthorized();

        // 일반 사용자 접근 시 예외처리
        if (user.getRole() != UserRole.ADMIN) throw AdminAccessException.accessDenied();

        reportAdminService.updateReport(reportId, req);
        return new ResTemplate<>(HttpStatus.OK, "콘텐츠 수정 성공", null);
    }

    @DeleteMapping("{reportId}")
    public ResTemplate<?> deleteReport(
            @PathVariable Long reportId,
            @AuthenticationPrincipal Users user
    ) {
        if (user == null) throw InvalidTokenException.unauthorized();

        // 일반 사용자 접근 시 예외처리
        if (user.getRole() != UserRole.ADMIN) throw AdminAccessException.accessDenied();

        reportAdminService.deleteReport(reportId);
        return new ResTemplate<>(HttpStatus.OK, "콘텐츠 삭제 성공", null);
    }
}
