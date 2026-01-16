package JJinBBang.app.global.ocr.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "ocr")
public class OcrProperties {

    private Clova clova = new Clova();

    @Getter
    @Setter
    public static class Clova {
        private String apiUrl;
        private String secretKey;
    }
}
