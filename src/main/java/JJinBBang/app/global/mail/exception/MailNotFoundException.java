package JJinBBang.app.global.mail.exception;

import JJinBBang.app.global.error.exception.NotFoundGroupException;

public class MailNotFoundException extends NotFoundGroupException {
    public MailNotFoundException(String message) {
        super(message);
    }

    public static MailNotFoundException notFoundAuthCode() {
        return new MailNotFoundException("인증 코드가 존재하지 않습니다.");
    }
}
