package JJinBBang.app.domain.user.service;

import org.springframework.web.multipart.MultipartFile;

public interface CertificateService {
    String uploadEnrollmentFileToDrive(MultipartFile file, String folderName);
    String uploadAdmissionFileToDrive(MultipartFile file, String folderName);

    void appendEnrollmentFileToSheets(int userId, int universityId, String fileName, String fileLink);
    void appendAdmissionFileToSheets(int userId, int universityId, String fileName, String fileLink);

    void updateVerificationStatusByCertificate(Long userId, String status);
}
