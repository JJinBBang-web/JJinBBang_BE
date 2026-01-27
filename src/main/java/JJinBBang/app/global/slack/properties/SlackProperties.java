package JJinBBang.app.global.slack.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "slack")
public class SlackProperties {

    private Webhook webhook = new Webhook();
    private Security security = new Security();

    @Getter
    @Setter
    public static class Webhook {
        private String verifyUrl;
        private String opinionUrl;
    }

    @Getter
    @Setter
    public static class Security {
        private String signingSecret;
    }
}
