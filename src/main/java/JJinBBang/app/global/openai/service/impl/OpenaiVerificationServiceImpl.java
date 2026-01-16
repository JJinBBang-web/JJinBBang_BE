package JJinBBang.app.global.openai.service.impl;

import JJinBBang.app.global.openai.properties.OpenaiProperties;
import JJinBBang.app.global.openai.service.OpenaiVerificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenaiVerificationServiceImpl implements OpenaiVerificationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final OpenaiProperties openaiProperties;

    private static final Pattern DRIVE_ID_PATTERN = Pattern.compile("/d/([a-zA-Z0-9_-]+)");

    @Override
    public boolean verifyCertificatesImage(String fileLink) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiProperties.getApiKey());

            // 1) 이미지 로드 및 Base64 인코딩
            String base64Image = downloadAndEncodeImage(fileLink);
            if (base64Image == null) {
                log.error("❌ 이미지 다운로드 실패로 인한 검증 중단");
                return false;
            }

            // 2) 프롬프트 생성
            String promptText = openaiProperties.getCertificatesVerificationPrompt();
            if (promptText == null || promptText.isBlank()) {
                promptText = "이 이미지가 유효한 대학 합격증명서인지, 위조된 흔적은 없는지 JSON 형태로 판단해줘.";
                log.warn("⚠️ 프롬프트 설정이 비어있어 기본 프롬프트를 사용합니다.");
            }

            // 3) Request body 생성
            Map<String, Object> req = new HashMap<>();
            req.put("model", openaiProperties.getModel());

            Map<String, Object> textContent = Map.of("type", "text", "text", promptText);
            Map<String, Object> imageContent = Map.of(
                    "type", "image_url",
                    "image_url", Map.of("url", "data:image/jpeg;base64," + base64Image)
            );

            req.put("messages", List.of(
                    Map.of("role", "system", "content", "You are a strict document verification AI. Output JSON only."),
                    Map.of("role", "user", "content", List.of(textContent, imageContent))
            ));
            req.put("response_format", Map.of("type", "json_object"));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(req, headers);

            // 4) API 호출
            String res = restTemplate.postForObject(openaiProperties.getApiUrl(), request, String.class);

            if (res == null || res.isBlank()) {
                log.error("🤖 OpenAI API 응답이 비어있습니다.");
                return false;
            }

            // 5) 결과 파싱
            JsonNode root = objectMapper.readTree(res);
            if (!root.has("choices") || root.path("choices").isEmpty()) {
                log.error("🤖 OpenAI 응답에 'choices' 필드가 없습니다. 응답 내용: {}", res);
                return false;
            }

            String content = root.path("choices").get(0).path("message").path("content").asText();
            JsonNode resultNode = objectMapper.readTree(content);

            boolean isValid = resultNode.path("isValid").asBoolean();
            String reason = resultNode.path("reason").asText();

            log.info("🤖 OpenAI 검증 결과: {} (이유: {})", isValid ? "통과" : "실패", reason);

            return isValid;
        } catch (RestClientException e) {
            log.error("🤖 OpenAI API 호출 중 네트워크/통신 오류 발생: {}", e.getMessage());
            return false; // 통신 오류 시 보수적으로 실패 처리
        } catch (JsonProcessingException e) {
            log.error("🤖 OpenAI 응답 JSON 파싱 실패", e);
            return false;
        } catch (Exception e) {
            log.error("🤖 LLM 검증 중 알 수 없는 오류 발생", e);
            return false;
        }
    }


    private String downloadAndEncodeImage(String fileLink) {
        try {
            // 1. 파일 ID 추출
            Matcher matcher = DRIVE_ID_PATTERN.matcher(fileLink);
            if (!matcher.find()) {
                log.error("유효하지 않은 구글 드라이브 링크입니다: {}", fileLink);
                return null;
            }
            String fileId = matcher.group(1);

            // 2. 다운로드 URL 생성 (export=download 사용)
            String downloadUrl = "https://drive.google.com/uc?export=download&id=" + fileId;

            // 3. 이미지 다운로드 (byte array)
            byte[] imageBytes = restTemplate.getForObject(downloadUrl, byte[].class);

            if (imageBytes == null || imageBytes.length == 0) {
                log.error("이미지 데이터가 비어있습니다.");
                return null;
            }

            // 4. Base64 인코딩
            return Base64.getEncoder().encodeToString(imageBytes);

        } catch (Exception e) {
            log.error("이미지 다운로드 및 인코딩 실패: {}", e.getMessage());
            return null;
        }
    }
}
