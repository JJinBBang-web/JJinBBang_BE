package JJinBBang.app.domain.user.service;

import org.springframework.web.multipart.MultipartFile;

public interface CertificateService {
    String uploadAdmissionFileToDrive(MultipartFile file, String folderName);

    void appendAdmissionFileToSheets(int userId, String fileName, String fileLink);

    void updateVerificationStatusByCertificate(Long userId, String status, String fileLink, String fileName);
}
