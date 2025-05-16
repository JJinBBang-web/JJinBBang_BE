package JJinBBang.app.global.mail.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "mail-auth")
public class MailAuthProperties {
    //mail-auth.expiration-time
    private long expirationTime;

    //mail-auth.auth-code-length
    private int authCodeLength;

    //mail-auth.allowed-domain
    private List<String> allowedDomain;

    //mail-auth.subject-text
    private String subjectText;

    //mail-auth.body-text
    private String bodyText;
}