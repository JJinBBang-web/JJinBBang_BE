package JJinBBang.app.global.ocr.service.impl;

import JJinBBang.app.global.ocr.dto.request.ClovaImage;
import JJinBBang.app.global.ocr.dto.request.ClovaOcrRequest;
import JJinBBang.app.global.ocr.dto.response.ClovaField;
import JJinBBang.app.global.ocr.dto.response.ClovaImageResult;
import JJinBBang.app.global.ocr.dto.response.ClovaOcrResponse;
import JJinBBang.app.global.ocr.properties.OcrProperties;
import JJinBBang.app.global.ocr.service.OcrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@RequiredArgsConstructor
@Slf4j
public class OcrServiceImpl implements OcrService {

    private final RestTemplate restTemplate;
    private final OcrProperties ocrProperties;

    @Override
    public String extractTextFromGoogleDrive(String imageUrl, String fileName) {
        try {

            String fileId = extractFileIdFromUrl(imageUrl);
            String downloadUrl = "https://drive.google.com/uc?export=download&id=" + fileId;

            // 확장자 판별
            String extension = getExtension(fileName);

            // 1) 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-OCR-SECRET", ocrProperties.getClova().getSecretKey());
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 2) Request Body 설정
            ClovaImage image = new ClovaImage(extension, fileName, downloadUrl);

            ClovaOcrRequest req = new ClovaOcrRequest(
                    "V2",
                    UUID.randomUUID().toString(),
                    System.currentTimeMillis(),
                    List.of(image)
            );

            HttpEntity<ClovaOcrRequest> requestEntity = new HttpEntity<>(req, headers);
            log.info("Clova OCR Request SUCCESS!");
            log.info("format: {}", extension);
            log.info("URL: {}", downloadUrl);

            ClovaOcrResponse res = restTemplate.postForObject(ocrProperties.getClova().getApiUrl(), requestEntity, ClovaOcrResponse.class);

            StringBuilder result = new StringBuilder();
            if (res != null && res.images() != null) {
                for (ClovaImageResult imageResult : res.images()) {
                    if (imageResult.fields() != null) {
                        for (ClovaField field : imageResult.fields()) {
                            result.append(field.inferText()).append(" ");
                        }
                    }
                }
            }

            log.info("OCR 추출 완료: {}", result);
            return result.toString().trim();

        } catch (Exception e) {
            throw new RuntimeException("OCR 처리 실패");
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null) return "jpg";

        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith("pdf")) return "pdf";
        if (lowerName.endsWith("png")) return "png";
        if (lowerName.endsWith("jpeg")) return "jpeg";

        return "jpg";
    }

    private String extractFileIdFromUrl(String url) {
        Pattern pattern = Pattern.compile("/file/d/([^/]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) return matcher.group(1);

        Pattern pattern2 = Pattern.compile("id=([^&]+)");
        Matcher matcher2 = pattern2.matcher(url);
        if (matcher2.find()) return matcher2.group(1);

        return null;
    }
}
