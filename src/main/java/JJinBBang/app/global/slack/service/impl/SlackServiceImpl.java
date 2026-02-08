package JJinBBang.app.global.slack.service.impl;

import JJinBBang.app.domain.common.entity.Universities;
import JJinBBang.app.domain.common.repository.UniversitiesRepository;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.exception.UserNotFoundException;
import JJinBBang.app.domain.user.repository.UsersRepository;
import JJinBBang.app.domain.user.service.CertificateService;
import JJinBBang.app.global.common.enums.VerificationStatus;
import JJinBBang.app.global.slack.enums.SlackNotificationType;
import JJinBBang.app.global.slack.properties.SlackProperties;
import JJinBBang.app.global.slack.service.SlackService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlackServiceImpl implements SlackService {

    private final SlackProperties slackProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CertificateService certificateService;
    private final UniversitiesRepository universitiesRepository;
    private final UsersRepository usersRepository;
    private final MethodsClient methodsClient;

    private static final String INPUT_BLOCK_ID = "input_university_block";
    private static final String ACTION_ID_INPUT = "university_name";
    private static final String ACTION_ID_APPROVE = "approve_student_verification";
    private static final String ACTION_ID_APPROVE_WITH_INPUT = "approve_student_verification_with_input";
    private static final String ACTION_ID_REJECT = "reject_student_verification";

    /**
     * Slack #합격증명서-인증 채널에 찐빵 봇을 통한 미승인 합격증명서 인증 메시지 전송
     *
     * @param userId
     * @param fileLink 합격증명서가 저장 된 Google Drive URL
     * @param needsInput OCR 단계에서 대학교 조회가 된 경우 - false / 조회가 되지 않은 경우 - true
     */
    @Override
    public void sendVerifyMessage(Long userId, String fileLink, boolean needsInput) {
        String webhookUrl = slackProperties.getWebhook().getVerifyUrl();

        // 1) 전체 메시지 Payload 생성
        Map<String, Object> payload = new HashMap<>();

        // 2) Slack 메시지 내용
        List<Map<String, Object>> blocks = new ArrayList<>();

        if (needsInput) {
            // CASE 1. 재학중인 대학교 추출에 실패한 경우
            payload.put("text", "새로 업로드 된 합격증명서 검증이 필요합니다! (대학교 수동 입력 필요)");
            blocks.add(createTextBlockByTypes(SlackNotificationType.CERTIFICATE, userId, fileLink));
            blocks.add(createInputBlock());
            blocks.add(createActionBlock(userId, ACTION_ID_APPROVE_WITH_INPUT));
        } else {
            // CASE 2. 재학중인 대학교 추출에 성공한 경우 (다른 검증 과정에서 오류가 발생한 경우)
            payload.put("text", "새로 업로드 된 합격증명서 검증이 필요합니다!");
            blocks.add(createTextBlockByTypes(SlackNotificationType.CERTIFICATE, userId, fileLink));
            blocks.add(createActionBlock(userId, ACTION_ID_APPROVE));
        }

        payload.put("blocks", blocks);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            restTemplate.postForEntity(webhookUrl, request, String.class);
            log.info("Slack 인증 메시지 전송 완료: ID {}", userId);
        } catch (RestClientException e) {
            log.error("Slack API 호출 중 네트워크 오류 발생: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Slack 메시지 전송 중 알 수 없는 오류 발생: ", e);
        }
    }

    /**
     * Slack에서 전송된 payload를 처리하고 승인/반려 작업 수행
     *
     * @param payload Slack에서 넘어온 JSON 형식의 payload
     * @return 처리 결과
     * @throws JsonProcessingException JSON 페이로드 파싱 과정에서 발생하는 도중 에러가 발생하는 경우
     */
    @Transactional
    @Override
    public String handleInteractivity(String payload) throws JsonProcessingException {

        // 1) Slack이 보낸 JSON 파싱
        JsonNode root = objectMapper.readTree(payload);

        String channelId = root.path("channel").path("id").asText();
        String threadTs = root.path("message").path("ts").asText();

        // 2) 사용자 정보, action 추출
        JsonNode actionsNode = root.path("actions").get(0);
        String actionId = actionsNode.path("action_id").asText();
        String value = actionsNode.path("value").asText();
        Long userId = Long.parseLong(value);

        JsonNode userNode = root.path("user");
        String adminId = userNode.path("id").asText();
        String adminTag = "<@" + adminId + ">";

        String message = "";

        // 3) 수동 인증 로직
        if (ACTION_ID_APPROVE_WITH_INPUT.equals(actionId)) {
            // CASE 1. 대학교 이름 수동 입력 승인
            String inputUnivName = root.path("state")
                    .path("values")
                    .path(INPUT_BLOCK_ID)
                    .path(ACTION_ID_INPUT)
                    .path("value")
                    .asText();

            // 대학교 조회 및 업데이트
            if (inputUnivName == null || inputUnivName.isBlank()) {
                return createResponse(false, "⚠️ 대학교 이름을 입력해주세요!");
            }

            Optional<Universities> univ = universitiesRepository.findByUniversityName(inputUnivName.trim());
            if (univ.isEmpty()) {
                return createResponse(false, "⚠️ '" + inputUnivName + "' 학교를 DB에서 찾을 수 없습니다.");
            }

            Users user = usersRepository.findByUserId(userId).orElseThrow(UserNotFoundException::notFound);
            user.updateUniversity(univ.get());

            certificateService.updateVerificationStatusByCertificate(
                    userId,
                    String.valueOf(VerificationStatus.NEW_STUDENT_VERIFIED),
                    null,
                    null
            );

            log.info("관리자 수동 입력 승인: User - {} / University - {}", userId, inputUnivName);
            message = "✅ 관리자(" + adminTag +")에 의해 승인 처리되었습니다.";

        } else if (ACTION_ID_APPROVE.equals(actionId)) {
            // CASE 2. 수동 승인
            certificateService.updateVerificationStatusByCertificate(
                    userId,
                    String.valueOf(VerificationStatus.NEW_STUDENT_VERIFIED),
                    null,
                    null
            );

            message = "✅ 관리자(" + adminTag + ")에 의해 승인 처리되었습니다.";
            log.info("Slack에서 승인 처리 완료: userId - {}", userId);
        } else if (ACTION_ID_REJECT.equals(actionId)) {
            // CASE 3. 수동 반려
            certificateService.updateVerificationStatusByCertificate(
                    userId,
                    String.valueOf(VerificationStatus.REJECTED),
                    null,
                    null
            );
            message = "❌ 관리자에(" + adminTag + ")에 의해 반려 처리되었습니다.";
            log.info("Slack에서 반려 처리 완료: userId - {}", userId);
        }

        // 승인 및 반려 시 스레드 메시지 전송
        postThreadMessage(channelId, threadTs, message);
        return createResponse(true, message);
    }

    // 텍스트 섹션 생성하기
    private Map<String, Object> createTextBlock(String content) {
        Map<String, Object> text = new HashMap<>();
        text.put("type", "mrkdwn");
        text.put("text", content);

        Map<String, Object> section = new HashMap<>();
        section.put("type", "section");
        section.put("text", text);

        return section;
    }

    private Map<String, Object> createTextBlockByTypes(SlackNotificationType type, Long userId, String data) {
        String formattedMessage = type.format(userId, data);
        return createTextBlock(formattedMessage);
    }

    // 의사 버튼 섹션 생성하기 (승인/반려)
    private Map<String, Object> createActionBlock(Long userId, String approveType) {
        Map<String, Object> actions = new HashMap<>();
        actions.put("type", "actions");

        List<Map<String, Object>> elements = new ArrayList<>();

        elements.add(createButton("승인", "primary", approveType, String.valueOf(userId)));
        elements.add(createButton("반려", "danger", ACTION_ID_REJECT, String.valueOf(userId)));

        actions.put("elements", elements);
        return actions;
    }

    // input block 생성하기
    private Map<String, Object> createInputBlock() {
        Map<String, Object> inputBlock = new HashMap<>();
        inputBlock.put("type", "input");
        inputBlock.put("block_id", INPUT_BLOCK_ID);
        inputBlock.put("label", Map.of("type", "plain_text", "text", "대학교 이름 입력"));

        Map<String, Object> element = new HashMap<>();
        element.put("type", "plain_text_input");
        element.put("action_id", ACTION_ID_INPUT);
        element.put("placeholder", Map.of("type", "plain_text", "text", "정확한 대학교 이름을 입력해주세요."));

        inputBlock.put("element", element);
        return inputBlock;
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
    private String createResponse(boolean replaceOriginal, String text) {
        Map<String, Object> response = new HashMap<>();
        response.put("replace_original", replaceOriginal);
        response.put("text", text);

        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            log.error("Slack 응답 JSON 생성 실패", e);
            return String.format("{\"replace_original\": \"%b\", \"text\": \"Error processing response\"}", replaceOriginal);
        }
    }

    // Slack Thread 메시지 전송
    private void postThreadMessage(String channelId, String threadTs, String message) {
        try {
            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    .channel(channelId)
                    .threadTs(threadTs)
                    .text(message)
                    .build();

            methodsClient.chatPostMessage(request);
            log.info("Slack 스레드 메시지 전송 완료: {}", threadTs);
        } catch (IOException | SlackApiException e) {
            log.error("Slack 스레드 메시지 전송 중 오류 발생: thread_ts={}", threadTs, e);
        }
    }


    @Override
    public void sendOpinionMessage(Long userId, String opinion) {
        String webhookUrl = slackProperties.getWebhook().getOpinionUrl();

        // 1) 전체 메시지 Payload 생성
        Map<String, Object> payload = new HashMap<>();

        // 2) Slack 메시지 내용
        List<Map<String, Object>> blocks = new ArrayList<>();
        payload.put("text", "유저 문의 및 신고가 접수되었습니다!");
        blocks.add(createTextBlockByTypes(SlackNotificationType.OPINION, userId, opinion));
        payload.put("blocks", blocks);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            restTemplate.postForEntity(webhookUrl, request, String.class);
            log.info("Slack 인증 메시지 전송 완료: ID {}", userId);
        } catch (RestClientException e) {
            log.error("Slack API 호출 중 네트워크 오류 발생: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Slack 메시지 전송 중 알 수 없는 오류 발생: ", e);
        }
    }
}
