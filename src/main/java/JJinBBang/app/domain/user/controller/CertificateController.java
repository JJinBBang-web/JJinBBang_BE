package JJinBBang.app.domain.user.controller;

import JJinBBang.app.domain.user.dto.request.UpdateVerificationStatusDto;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.service.CertificateService;
import JJinBBang.app.domain.user.service.UsersService;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;
    private final UsersService usersService;

    // 재학증명서 업로드
    @PostMapping("/enrollment/verify")
    public ResTemplate<?> uploadEnrollmentCertificate(
            @AuthenticationPrincipal Users principal,
            @RequestPart("file") MultipartFile file
    ) {
        Users user = usersService.findWithUniversity(principal.getProviderId());

        // 대학교 이름을 기준으로 폴더 지정
        String folderName = user.getUniversity().getUniversityName();

        // 구글 드라이브에 업로드 및 링크 반환
        String fileLink = certificateService.uploadEnrollmentFileToDrive(file, folderName);

        // 스프레드 시트에 row로 업로드 (userId, universityId, 파일링크, 업로드 시간)
        certificateService.appendEnrollmentFileToSheets(
                user.getUserId().intValue(),
                user.getUniversity().getId().intValue(),
                file.getOriginalFilename(),
                fileLink
        );

        return new ResTemplate<>(HttpStatus.OK,
                "업로드 성공",
                null);
    }

    // 합격증명서 업로드
    @PostMapping("/admission/verify")
    public ResTemplate<?> uploadAdmissionCertificate(
            @AuthenticationPrincipal Users principal,
            @RequestPart("file") MultipartFile file
    ) {
        Users user = usersService.findWithUniversity(principal.getProviderId());

        // 구글 드라이브에 업로드 및 링크 반환
        String folderName = user.getUniversity().getUniversityName();

        // 구글 드라이브에 업로드 및 링크 반환
        String fileLink = certificateService.uploadAdmissionFileToDrive(file, folderName);

        // 스프레드 시트에 row로 업로드 (userId, universityId, 파일링크, 업로드 시간)
        certificateService.appendAdmissionFileToSheets(
                user.getUserId().intValue(),
                user.getUniversity().getId().intValue(),
                file.getOriginalFilename(),
                fileLink
        );

        return new ResTemplate<>(
                HttpStatus.OK,
                "업로드 성공",
                null
        );
    }
}