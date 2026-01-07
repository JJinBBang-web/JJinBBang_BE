package JJinBBang.app.global.slack.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.transaction.annotation.Transactional;

public interface SlackService {

    void sendVerifyMessage(Long userId, String fileLink);

    String handleInteractivity(String payload) throws JsonProcessingException;
}
