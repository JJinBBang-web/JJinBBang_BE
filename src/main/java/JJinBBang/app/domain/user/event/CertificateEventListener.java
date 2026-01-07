package JJinBBang.app.domain.user.event;

import JJinBBang.app.domain.user.service.CertificateService;
import JJinBBang.app.global.common.enums.VerificationStatus;
import JJinBBang.app.global.slack.service.SlackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class CertificateEventListener {

    private final SlackService slackService;
    private final CertificateService certificateService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 트랜잭선 커밋 후 실행
    public void handleCertificateVerificationEvent(CertificateUploadEvent event) {
        log.info("합격증명서 검증 프로세스 시작: USER {}", event.userId());

        try {
            // TODO: OCR & 검증 작업

            boolean isAutoVerified = false;

            if (isAutoVerified) {
                // CASE 1) 자동 검증 성공 -> 신입생 인증으로 변경
                certificateService.updateVerificationStatusByCertificate(
                        event.userId(),
                        String.valueOf(VerificationStatus.NEW_STUDENT_VERIFIED),
                        null
                );
            } else {
                // CASE 2) 자동 검증 실패 -> Slack 알림 전송 (관리자 수동 검증 필요)
                log.info("승인 실패 - Slack에서 관리자 수동 인증을 진행합니다.");
                slackService.sendVerifyMessage(event.userId(), event.fileLink());
            }
        } catch (Exception e) {
            log.error("검증 처리 중 오류 발생", e);
            certificateService.updateVerificationStatusByCertificate(
                    event.userId(),
                    String.valueOf(VerificationStatus.UNVERIFIED),
                    null
            );
        }
    }
}
