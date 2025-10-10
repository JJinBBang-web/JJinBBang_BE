package JJinBBang.app.global.sheets.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Component
@ConfigurationProperties(prefix = "google")
public class GoogleProperties {
    private Drive drive = new Drive();

    private Spreadsheet spreadsheetCertificate = new Spreadsheet(); // 중명서 시트

    private Spreadsheet spreadsheetUnregister = new Spreadsheet(); // 탈퇴 사유 시트
    private Spreadsheet spreadsheetOpinion = new Spreadsheet(); // 문의(신고) 시트

    @Setter
    @Getter
    public static class Drive {
        private Map<String, String> folders;
        private String urlTemplate;
    }

    @Setter
    @Getter
    public static class Spreadsheet {
        private String id;
        private Map<String, String> sheets;
        private String rangeTemplate;
    }
}