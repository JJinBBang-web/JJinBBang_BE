package JJinBBang.app.global.config;

import JJinBBang.app.global.sheets.properties.GoogleProperties;
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
import org.springframework.core.io.Resource;

import java.util.List;

@Configuration
public class GoogleApiConfig {

    @Value("${google.credentials.path}")
    private Resource credentialsPath;

    private final GoogleProperties props;

    public GoogleApiConfig(GoogleProperties props) {
        this.props = props;
    }

    @Bean
    public Drive googleDrive() throws Exception {
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        var creds = ServiceAccountCredentials
                .fromStream(credentialsPath.getInputStream())
                .createScoped(List.of(
                        DriveScopes.DRIVE_FILE,
                        DriveScopes.DRIVE_METADATA
                ));

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                jsonFactory,
                new HttpCredentialsAdapter(creds)
        )
                .setApplicationName("JJinBBangDriveService")
                .build();
    }

    @Bean
    public Sheets googleSheets() throws Exception {
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        var creds = ServiceAccountCredentials
                .fromStream(credentialsPath.getInputStream())
                .createScoped(List.of(
                        SheetsScopes.SPREADSHEETS
                ));

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                jsonFactory,
                new HttpCredentialsAdapter(creds)
        )
                .setApplicationName("JJinBBangSheetsService")
                .build();
    }
}
