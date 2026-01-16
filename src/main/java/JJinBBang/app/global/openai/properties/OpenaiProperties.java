package JJinBBang.app.global.openai.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "openai")
public class OpenaiProperties {

    private String apiKey;
    private String model;
    private String apiUrl;
    private String certificatesVerificationPrompt;
}
