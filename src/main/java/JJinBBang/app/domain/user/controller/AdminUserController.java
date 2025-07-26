package JJinBBang.app.domain.user.controller;

import JJinBBang.app.domain.user.dto.request.UpdateVerificationStatusDto;
import JJinBBang.app.domain.user.service.CertificateService;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final CertificateService certificateService;

    /**
     * Admin Controller
     * 사용자 인증 상태 변경
     */
    @PatchMapping("/verification/updateStatus")
    public ResTemplate<?> updateVerificationStatus(
            @RequestBody UpdateVerificationStatusDto updateVerificationStatus
    ){

        certificateService.updateVerificationStatusByCertificate(
                updateVerificationStatus.userId(),
                updateVerificationStatus.verificationStatus()
        );

        return new ResTemplate<>(
                HttpStatus.OK,
                "증명서 인증 상태 변경",
                null
        );
    }
}
