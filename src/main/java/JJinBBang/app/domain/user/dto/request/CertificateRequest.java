package JJinBBang.app.domain.user.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record CertificateRequest(
        MultipartFile certificate,
        int universityId,
        String studentNumber
) {
}