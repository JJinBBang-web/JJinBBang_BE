package JJinBBang.app.global.slack.service;

public interface SlackRequestVerifier {

    boolean isValid(String timestamp, String signature, String body);
}
