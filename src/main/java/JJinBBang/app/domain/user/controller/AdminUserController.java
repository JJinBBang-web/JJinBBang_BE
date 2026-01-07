package JJinBBang.app.domain.user.controller;

import JJinBBang.app.domain.user.dto.request.UpdateVerificationStatusDto;
import JJinBBang.app.domain.user.service.CertificateService;
import JJinBBang.app.domain.user.service.UsersService;
import JJinBBang.app.global.slack.service.SlackService;
import JJinBBang.app.global.template.ResTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final CertificateService certificateService;
    private final UsersService usersService;
    private final SlackService slackService;

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
                updateVerificationStatus.verificationStatus(),
                null
        );

        return new ResTemplate<>(
                HttpStatus.OK,
                "증명서 인증 상태 변경",
                null
        );
    }

    @PostMapping("/slack/verification")
    public ResTemplate<?> handleInteraction(
            @RequestParam("payload") String payload
    ) throws JsonProcessingException {
        return new ResTemplate<>(HttpStatus.OK, slackService.handleInteractivity(payload), null);
    }

    @DeleteMapping("/deletedUsers/executeDeletion")
    public ResTemplate<?> executeUserDeletion() {
        usersService.forceDeleteExecute();
        return new ResTemplate<>(
                HttpStatus.OK,
                "탈퇴한 유저의 데이터 영구 삭제 완료",
                null
        );
    }


}
