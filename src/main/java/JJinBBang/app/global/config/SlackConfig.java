package JJinBBang.app.global.config;

import JJinBBang.app.global.slack.properties.SlackProperties;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackConfig {

    private final SlackProperties slackProperties;

    public SlackConfig(SlackProperties slackProperties) {
        this.slackProperties = slackProperties;
    }

    @Bean
    public MethodsClient methodsClient() {
        return Slack.getInstance().methods(slackProperties.getBotToken());
    }
}
