package JJinBBang.app.global.mail.exception;

import JJinBBang.app.global.error.exception.AuthGroupException;

public class MailAuthException extends AuthGroupException {
    public MailAuthException(String message) {
        super(message);
    }

    public static MailAuthException notMatchAuthCode() {
        return new MailAuthException("인증 코드가 일치하지 않습니다.");
    }
}
