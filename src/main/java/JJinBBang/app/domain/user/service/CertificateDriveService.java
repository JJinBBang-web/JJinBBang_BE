package JJinBBang.app.domain.user.service;

import JJinBBang.app.domain.user.exception.CertificateBadRequestException;
import JJinBBang.app.domain.user.exception.CertificateProcessException;
import JJinBBang.app.domain.user.exception.UserAuthException;
import JJinBBang.app.global.error.exception.UnprocessableGroupException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;

public class CertificateDriveService {
    private final Drive drive;
    public final String folderId;

    public CertificateDriveService(Drive drive, String folderId) {
        this.drive = drive;
        this.folderId = folderId;
    }

    public String uploadPdf(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw CertificateBadRequestException.PDFUploadException();
        }

        try {
            var metadata = new File()
                    .setName(file.getOriginalFilename())
                    .setParents(Collections.singletonList(folderId));

            var content = new InputStreamContent(
                    file.getContentType(),
                    file.getInputStream()
            );

            var uploadedFile = drive.files()
                    .create(metadata, content)
                    .setFields("id")
                    .execute();

            String fileId = uploadedFile.getId();

            drive.permissions()
                    .create(fileId, new Permission().setType("anyone").setRole("reader"))
                    .execute();

            return "https://drive.google.com/file/d/" + fileId + "/view?usp=sharing";
        } catch (GoogleJsonResponseException e) {
            switch (e.getStatusCode()) {
                case 400:
                    throw CertificateBadRequestException.DriveAPIException();
                case 401:
                    throw UserAuthException.InvalidToken();
                default:
                    throw CertificateProcessException.ProcessException();
            }
        } catch (IOException e) {
            throw new UnprocessableGroupException("파일 처리 중 오류가 발생했습니다.");
        }





    }
}
