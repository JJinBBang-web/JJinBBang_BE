package JJinBBang.app.global.sheets.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "google")
public class GoogleProperties {

    private Drive drive = new Drive();
    private Spreadsheet spreadsheet = new Spreadsheet();

    @Setter
    @Getter
    public static class Drive {
        private Map<String, String> folders = new HashMap<>();
        private String urlTemplate;
    }

    @Getter
    @Setter
    public static class Spreadsheet {
        private SheetConfig certificates = new SheetConfig();
        private SheetConfig opinion = new SheetConfig();
        private SheetConfig unregister = new SheetConfig();
    }

    @Setter
    @Getter
    public static class SheetConfig {
        private String id;
        private Map<String, String> sheets = new HashMap<>();
        private String rangeTemplate;
    }
}