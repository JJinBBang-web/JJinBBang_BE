package JJinBBang.app.global.config;

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
    private Spreadsheet spreadsheet = new Spreadsheet();

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