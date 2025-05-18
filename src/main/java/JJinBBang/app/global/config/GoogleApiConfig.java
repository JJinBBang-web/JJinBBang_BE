package JJinBBang.app.global.config;

import JJinBBang.app.domain.user.service.CertificateDriveService;
import JJinBBang.app.domain.user.service.CertificateSheetsService;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Configuration
public class GoogleApiConfig {
    @Value("${google.drive.folder-id}")
    private String driveFolderId;

    @Value("${google.spreadsheet.id}")
    private String spreadsheetId;

    @Bean
    public Drive googleDrive() throws IOException, GeneralSecurityException {
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        var credentials = ServiceAccountCredentials.fromStream(
                new ClassPathResource("google.json").getInputStream()
        ).createScoped(List.of(
                DriveScopes.DRIVE_FILE,
                DriveScopes.DRIVE_METADATA
        ));

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                jsonFactory,
                new HttpCredentialsAdapter(credentials)
        )
                .setApplicationName("CertificateDrive")
                .build();
    }

    @Bean
    public Sheets googleSheets() throws IOException, GeneralSecurityException {
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        var credentials = ServiceAccountCredentials.fromStream(
                new ClassPathResource("google.json").getInputStream() // google 서비스 인증 키 (수정필요)
        ).createScoped(List.of(SheetsScopes.SPREADSHEETS));

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                jsonFactory,
                new HttpCredentialsAdapter(credentials)
        )
                .setApplicationName("CertificateSheets")
                .build();
    }

    @Bean
    public CertificateDriveService CertificateDriveService(Drive googleDrive) {
        return new CertificateDriveService(googleDrive, driveFolderId);
    }

    @Bean
    public CertificateSheetsService CertificateSheetsService(Sheets googleSheets) {
        return new CertificateSheetsService(googleSheets, spreadsheetId);
    }
}
