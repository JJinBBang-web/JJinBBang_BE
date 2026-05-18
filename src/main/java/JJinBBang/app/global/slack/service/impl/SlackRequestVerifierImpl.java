package JJinBBang.app.global.slack.service.impl;

import JJinBBang.app.global.slack.properties.SlackProperties;
import JJinBBang.app.global.slack.service.SlackRequestVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class SlackRequestVerifierImpl implements SlackRequestVerifier {

    private static final String VERSION = "v0";
    private static final long ALLOWED_CLOCK_SKEW_SECONDS = 60L * 5L;

    private final SlackProperties slackProperties;
    private final Clock clock;

    @Override
    public boolean isValid(String timestamp, String signature, String body) {
        String signingSecret = slackProperties.getSecurity().getSigningSecret();
        if (signingSecret == null || signingSecret.isBlank()
                || timestamp == null || signature == null || body == null) {
            return false;
        }
        if (isExpired(timestamp)) {
            return false;
        }

        String expectedSignature = VERSION + "=" + hmacSha256(
                signingSecret,
                VERSION + ":" + timestamp + ":" + body
        );
        return constantTimeEquals(expectedSignature, signature);
    }

    private boolean isExpired(String timestamp) {
        try {
            long requestEpochSeconds = Long.parseLong(timestamp);
            long nowEpochSeconds = Instant.now(clock).getEpochSecond();
            return Math.abs(nowEpochSeconds - requestEpochSeconds) > ALLOWED_CLOCK_SKEW_SECONDS;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private String hmacSha256(String signingSecret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    signingSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Slack request signature verification failed", e);
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = actual.getBytes(StandardCharsets.UTF_8);

        if (expectedBytes.length != actualBytes.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < expectedBytes.length; i++) {
            result |= expectedBytes[i] ^ actualBytes[i];
        }
        return result == 0;
    }
}
