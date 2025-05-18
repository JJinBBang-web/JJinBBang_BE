package JJinBBang.app.domain.user.service;

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

    public String uploadPdf(MultipartFile file) throws IOException {
        var meta = new File()
                .setName(file.getOriginalFilename())
                .setParents(Collections.singletonList(folderId));

        var content = new InputStreamContent(
                file.getContentType(),
                file.getInputStream()
        );

        var uploadedFile = drive.files()
                .create(meta, content)
                .setFields("id")
                .execute();

        String fileId = uploadedFile.getId();

        drive.permissions()
                .create(fileId, new Permission().setType("anyone").setRole("reader"))
                .execute();

        return "https://drive.google.com/file/d/" + fileId + "/view?usp=sharing";
    }
}
