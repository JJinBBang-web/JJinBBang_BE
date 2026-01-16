package JJinBBang.app.domain.user.event;

import JJinBBang.app.domain.common.entity.Universities;
import JJinBBang.app.domain.common.repository.UniversitiesRepository;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.exception.UserNotFoundException;
import JJinBBang.app.domain.user.repository.UsersRepository;
import JJinBBang.app.domain.user.service.CertificateService;
import JJinBBang.app.global.common.enums.VerificationStatus;
import JJinBBang.app.global.ocr.dto.response.OcrResult;
import JJinBBang.app.global.ocr.service.OcrService;
import JJinBBang.app.global.openai.service.OpenaiVerificationService;
import JJinBBang.app.global.slack.service.SlackService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class CertificateEventListener {

    private final SlackService slackService;
    private final CertificateService certificateService;
    private final OcrService ocrService;
    private final UniversitiesRepository universitiesRepository;
    private final UsersRepository usersRepository;
    private final OpenaiVerificationService openaiVerificationService;

    private List<Universities> universityCache;

    private static final double CONFIDENCE_THRESHOLD = 0.9; // 신뢰도 기준값
    private static final String[] ADMISSION_KEYWORDS = {"합격", "입학", "admission"};
    private static final String[] AUTHORITY_KEYWORDS = {"총장", "학장", "처장", "직인"};

    @PostConstruct
    public void initUniversityCache() {
        this.universityCache = universitiesRepository.findAll();
        this.universityCache.sort(Comparator.comparing((Universities u) -> u.getUniversityName().length()).reversed());
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 트랜잭선 커밋 후 실행
    public void handleCertificateVerificationEvent(CertificateUploadEvent event) {
        log.info("합격증명서 검증 프로세스 시작: USER {}", event.userId());

        try {
            // 1) execute OCR
            OcrResult ocrResult = ocrService.extractTextFromGoogleDrive(event.fileLink(), event.fileName());
            String extractedText = ocrResult.text();
            double confidence = ocrResult.confidence();

            log.info("✨ OCR 결과 : {}", extractedText);
            log.info("✨ OCR 결과 신뢰도: {}", confidence);

            // 1) OCR 결과 전처리
            String cleanText = extractedText.replaceAll("\\s+", "").toLowerCase();

            // 2) 대학교 조회
//            Optional<Universities> university = findUniversityFromText(cleanText);
            Optional<Universities> university = findUniversityFromTextInMemory(cleanText);
            
            if (university.isEmpty()) {
                // CASE 1. 자동 검증 실패 - 대학교 조회에 실패한 경우
                log.warn("❌ 소속 대학을 조회할 수 없음");
                slackService.sendVerifyMessage(event.userId(), event.fileLink(), true);
                return;
            }

            Universities univ = university.get();
            log.info("소속 대학교: {}", univ.getUniversityName());

            // 3) 대학교 정보 업데이트
            Users user = usersRepository.findByUserId(event.userId()).orElseThrow(UserNotFoundException::notFound);
            user.updateUniversity(univ);

            boolean isVerified = performOcrResultVerification(cleanText, event.fileLink(), confidence);

            if (isVerified) {
                // CASE 2) 자동 검증 성공 -> 신입생 인증으로 변경
                certificateService.updateVerificationStatusByCertificate(
                        event.userId(),
                        String.valueOf(VerificationStatus.NEW_STUDENT_VERIFIED),
                        null,
                        null
                );
            } else {
                // CASE 3) 자동 검증 실패 -> Slack 알림 전송 (관리자 수동 검증 필요)
                log.info("승인 실패 - Slack에서 관리자 수동 인증을 진행합니다.");
                slackService.sendVerifyMessage(event.userId(), event.fileLink(), false);
            }
        } catch (Exception e) {
            log.error("검증 처리 중 오류 발생", e);
            certificateService.updateVerificationStatusByCertificate(
                    event.userId(),
                    String.valueOf(VerificationStatus.UNVERIFIED),
                    null,
                    event.fileName()
            );

            // 에러 발생 시 Slack 알림 전송
            slackService.sendVerifyMessage(event.userId(), event.fileLink(), false);
        }
    }

    private Optional<Universities> findUniversityFromTextInMemory(String text) {
        return universityCache.stream()
                .filter(univ -> text.contains(univ.getUniversityName()))
                .findFirst();
    }


    private boolean performOcrResultVerification(String text, String fileLink, double confidence) {
        if (text == null || text.isBlank()) return false;

        // 1) 신뢰도 검사 (OCR 품질 검증)
        if (confidence < CONFIDENCE_THRESHOLD) {
            log.warn("검증 실패 [1/4]: 추출된 내용의 신뢰도 낮음 ({})", confidence);
            return false;
        }

        // 2) 키워드 검사 (합격이나 입학에 관련된 서류인가?)
        if (!hasKeyword(text, ADMISSION_KEYWORDS)) {
            log.warn("검증 실패 [2/4]: 합격 관련 키워드가 존재하지 않음");
            return false;
        }

        // 3) 공문서 검사 (총장 혹은 입학처장 등으로 부터 발급된 서류인가?
        if (!hasKeyword(text, AUTHORITY_KEYWORDS)) {
            log.warn("검증 실패 [3/4]: 발급 주체가 존재하지 않음");
            return false;
        }

        // 4) OPENAI 문서 신뢰도 점검
        boolean isAiVerified = openaiVerificationService.verifyCertificatesImage(fileLink);
        if (!isAiVerified) {
            log.warn("검증 실패 [4/4]: LLM이 비정상 문서로 판정함");
            return false;
        }

        log.info("🔎 검증 통과!");
        log.info("OCR 신뢰도: {}", confidence);
        return true;
    }

    // 키워드 검사
    private boolean hasKeyword(String text, String[] keywords) {
        for (String k : keywords) {
            if (text.contains(k)) return true;
        }
        return false;
    }
}