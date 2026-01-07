package JJinBBang.app.global.slack.service.impl;

import JJinBBang.app.domain.user.service.CertificateService;
import JJinBBang.app.global.common.enums.VerificationStatus;
import JJinBBang.app.global.slack.properties.SlackProperties;
import JJinBBang.app.global.slack.service.SlackService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlackServiceImpl implements SlackService {

    private final SlackProperties slackProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CertificateService certificateService;

    /**
     * Slack #합격증명서-인증 채널에 찐빵 봇을 통한 미승인 합격증명서 인증 메시지 전송
     *
     * @param userId
     * @param fileLink 합격증명서가 저장 된 Google Drive URL
     */
    @Override
    public void sendVerifyMessage(Long userId, String fileLink) {
        String webhookUrl = slackProperties.getWebhook().getUrl();

        // 1) 전체 메시지 Payload 생성
        Map<String, Object> payload = new HashMap<>();
        payload.put("text", "새로 업로드 된 합격증명서 검증이 필요합니다!");

        // 2) Slack 메시지 내용
        List<Map<String, Object>> blocks = new ArrayList<>();
        blocks.add(createTextBlock(userId, fileLink)); //텍스트 섹션 생성
        blocks.add(createActionBlock(userId)); // 의사 버튼 생성

        payload.put("blocks", blocks);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            restTemplate.postForEntity(webhookUrl, request, String.class);
            log.info("Slack 인증 메시지 전송 완료: ID {}", userId);
        } catch (Exception e) {
            log.error("Slack 메시지 전송 실패: ", e);
        }
    }

    @Transactional
    @Override
    public String handleInteractivity(String payload) throws JsonProcessingException {

        // 1) Slack이 보낸 JSON 파싱
        JsonNode root = objectMapper.readTree(payload);

        // 2) 사용자 정보, action 추출
        JsonNode actionsNode = root.path("actions").get(0);
        String actionId = actionsNode.path("action_id").asText();
        String value = actionsNode.path("value").asText();
        Long userId = Long.parseLong(value);

        String message = "";

        if ("approve_student_verification".equals(actionId)) {
            certificateService.updateVerificationStatusByCertificate(
                    userId,
                    String.valueOf(VerificationStatus.NEW_STUDENT_VERIFIED),
                    null
            );

            message = "✅ 관리자에 의해 승인 처리되었습니다.";
            log.info("Slack에서 승인 처리 완료: userId - {}", userId);
        } else if ("reject_student_verification".equals(actionId)) {
            certificateService.updateVerificationStatusByCertificate(
                    userId,
                    String.valueOf(VerificationStatus.UNVERIFIED),
                    null
            );
            message = "❌ 관리자에 의해 반려 처리되었습니다.";
            log.info("Slack에서 반려 처리 완료: userId - {}", userId);
        }

        return createResponseJson(message);
    }

    // 텍스트 섹션 생성하기
    private Map<String, Object> createTextBlock(Long userId, String fileLink) {
        Map<String, Object> section = new HashMap<>();
        section.put("type", "section");

        Map<String, Object> text = new HashMap<>();
        text.put("type", "mrkdwn");
        text.put("text", String.format("[재학생 인증 요청 - ID: %s] \n관리자 확인이 필요합니다.\n\n📄 <%s|합격증명서 확인하기>", userId, fileLink));

        section.put("text", text);

        return section;
    }

    // 의사 버튼 섹션 생성하기 (승인/반려)
    private Map<String, Object> createActionBlock(Long userId) {
        Map<String, Object> actions = new HashMap<>();
        actions.put("type", "actions");

        List<Map<String, Object>> elements = new ArrayList<>();

        elements.add(createButton("승인", "primary", "approve_student_verification", String.valueOf(userId)));
        elements.add(createButton("반려", "danger", "reject_student_verification", String.valueOf(userId)));

        actions.put("elements", elements);
        return actions;
    }

    // 버튼 생성하기
    private Map<String, Object> createButton(String text, String style, String actionId, String value) {
        Map<String, Object> button = new HashMap<>();
        button.put("type", "button");
        button.put("text", Map.of("type", "plain_text", "text", text));
        button.put("style", style);
        button.put("action_id", actionId);
        button.put("value", value);

        return button;
    }

    // Slack 응답 메시지 포맷
    private String createResponseJson(String text) {
        return String.format("{\"replace_original\": \"true\", \"text\": \"%s\"}", text);
    }
}
