package JJinBBang.app.domain.user.controller;

import JJinBBang.app.domain.user.dto.request.CertificateRequest;
import JJinBBang.app.domain.user.service.CertificateDriveService;
import JJinBBang.app.domain.user.service.CertificateSheetsService;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateDriveService certificateDriveService;
    private final CertificateSheetsService certificateSheetsService;

    @PostMapping(value = "/enrollment/verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResTemplate<?> uploadEnrollmentCertificate(@ModelAttribute CertificateRequest certificateRequest) throws IOException {
        String sharedLink = certificateDriveService.uploadPdf(certificateRequest.certificate());

        certificateSheetsService.appendCertificateSheet(
                "jb_enrollment_verify_sheet", // 시트 이름 (수정 필요)
                certificateRequest.universityId(),
                certificateRequest.studentNumber(),
                certificateRequest.certificate().getOriginalFilename(),
                sharedLink
        );

        return new ResTemplate<>(
                HttpStatus.OK,
                "업로드 성공",
                sharedLink
        );
    }

    @PostMapping(value = "/admission/verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResTemplate<?> uploadAdmissionCertificate(@ModelAttribute CertificateRequest certificateRequest) throws IOException {
        String sharedLink = certificateDriveService.uploadPdf(certificateRequest.certificate());

        certificateSheetsService.appendCertificateSheet(
                "jb_admission_verify_sheet", // 시트 이름 (수정 필요)
                certificateRequest.universityId(),
                certificateRequest.studentNumber(),
                certificateRequest.certificate().getOriginalFilename(),
                sharedLink
        );

        return new ResTemplate<>(
                HttpStatus.OK,
                "업로드 성공",
                sharedLink
        );
    }
}