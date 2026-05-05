package JJinBBang.app.global.slack;

import JJinBBang.app.global.slack.properties.SlackProperties;
import JJinBBang.app.global.slack.service.impl.SlackRequestVerifierImpl;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlackRequestVerifierImplTest {

    private static final String SECRET = "test-signing-secret";
    private static final String TIMESTAMP = "1710000000";
    private static final String BODY = "payload=%7B%22type%22%3A%22block_actions%22%7D";

    private final SlackRequestVerifierImpl verifier = new SlackRequestVerifierImpl(
            slackProperties(),
            Clock.fixed(Instant.ofEpochSecond(1710000000), ZoneOffset.UTC)
    );

    @Test
    void isValid_acceptsMatchingSignatureWithinAllowedWindow() {
        String signature = sign(TIMESTAMP, BODY);

        assertTrue(verifier.isValid(TIMESTAMP, signature, BODY));
    }

    @Test
    void isValid_rejectsMismatchedSignature() {
        assertFalse(verifier.isValid(TIMESTAMP, "v0=invalid", BODY));
    }

    @Test
    void isValid_rejectsExpiredTimestamp() {
        String expiredTimestamp = "1709999000";
        String signature = sign(expiredTimestamp, BODY);

        assertFalse(verifier.isValid(expiredTimestamp, signature, BODY));
    }

    @Test
    void isValid_rejectsMissingSigningSecret() {
        SlackProperties properties = slackProperties();
        properties.getSecurity().setSigningSecret(null);
        SlackRequestVerifierImpl verifier = new SlackRequestVerifierImpl(
                properties,
                Clock.fixed(Instant.ofEpochSecond(1710000000), ZoneOffset.UTC)
        );

        assertFalse(verifier.isValid(TIMESTAMP, sign(TIMESTAMP, BODY), BODY));
    }

    private SlackProperties slackProperties() {
        SlackProperties properties = new SlackProperties();
        SlackProperties.Security security = new SlackProperties.Security();
        security.setSigningSecret(SECRET);
        properties.setSecurity(security);
        return properties;
    }

    private String sign(String timestamp, String body) {
        try {
            String baseString = "v0:" + timestamp + ":" + body;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return "v0=" + HexFormat.of().formatHex(mac.doFinal(baseString.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
