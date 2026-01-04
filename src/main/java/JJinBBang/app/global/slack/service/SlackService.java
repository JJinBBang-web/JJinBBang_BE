package JJinBBang.app.global.slack.service;

public interface SlackService {

    void sendVerifyMessage(Long userId, String fileLink);
}
