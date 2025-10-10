package JJinBBang.app.domain.user.controller;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.service.CertificateService;
import JJinBBang.app.domain.user.service.UsersService;
import JJinBBang.app.global.common.enums.VerificationStatus;
import JJinBBang.app.global.sheets.properties.GoogleProperties;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;
    private final UsersService usersService;
    private final GoogleProperties googleProps;

    // 재학증명서 업로드
    @PostMapping("/enrollment/verify")
    public ResTemplate<?> uploadEnrollmentCertificate(
            @AuthenticationPrincipal Users principal,
            @RequestPart("file") MultipartFile file
    ) {
        if (principal == null || principal.getProviderId() == null) {
            throw new RuntimeException("사용자 인증 정보가 유효하지 않습니다.");
        }
        try {
            Users user = usersService.findWithUniversity(principal.getProviderId());

            // 폴더 이름으로 타겟 폴더 지정
            String folderName = googleProps.getDrive().getFolders().get("enrollment-target");

            // 구글 드라이브에 업로드 및 링크 반환
            String fileLink = certificateService.uploadEnrollmentFileToDrive(file, folderName);

            // 스프레드 시트에 row로 업로드 (userId, universityId, 파일링크, 업로드 시간)
            certificateService.appendEnrollmentFileToSheets(
                    user.getUserId().intValue(),
                    file.getOriginalFilename(),
                    fileLink
            );

            // 미인증 -> 인증대기
            certificateService.updateVerificationStatusByCertificate(
                    user.getUserId(),
                    String.valueOf(VerificationStatus.PENDING)
            );

            return new ResTemplate<>(HttpStatus.OK,
                    "업로드 성공",
                    null);

        } catch (Exception e) {
            return new ResTemplate<>(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "업로드 중 문제가 발생했습니다.",
                    null
            );
        }
    }

    // 합격증명서 업로드
    @PostMapping("/admission/verify")
    public ResTemplate<?> uploadAdmissionCertificate(
            @AuthenticationPrincipal Users principal,
            @RequestPart("file") MultipartFile file
    ) {
        if (principal == null || principal.getProviderId() == null) {
            throw new RuntimeException("사용자 인증 정보가 유효하지 않습니다.");
        }

        try {
            Users user = usersService.findWithUniversity(principal.getProviderId());

            // 구글 드라이브에 업로드 및 링크 반환
            String folderName = googleProps.getDrive().getFolders().get("admission-target");

            // 구글 드라이브에 업로드 및 링크 반환
            String fileLink = certificateService.uploadAdmissionFileToDrive(file, folderName);

            // 스프레드 시트에 row로 업로드 (userId, universityId, 파일링크, 업로드 시간)
            certificateService.appendAdmissionFileToSheets(
                    user.getUserId().intValue(),
                    file.getOriginalFilename(),
                    fileLink
            );

            // 미인증 -> 인증대기
            certificateService.updateVerificationStatusByCertificate(
                    user.getUserId(),
                    String.valueOf(VerificationStatus.PENDING)
            );

            return new ResTemplate<>(
                    HttpStatus.OK,
                    "업로드 성공",
                    null
            );
        }  catch (Exception e) {
            return new ResTemplate<>(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "업로드 중 문제가 발생했습니다.",
                    null
            );
        }
    }
}