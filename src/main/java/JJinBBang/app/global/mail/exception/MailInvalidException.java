package JJinBBang.app.global.mail.exception;

import JJinBBang.app.global.error.exception.InvalidGroupException;

public class MailInvalidException extends InvalidGroupException {
    public MailInvalidException(String message) {
        super(message);
    }

    public static MailInvalidException invalidEmailFormat() {
        return new MailInvalidException("유효하지 않은 이메일 형식 입니다.");
    }

    public static MailInvalidException invalidEmailDomain() {
        return new MailInvalidException("유효하지 않은 이메일 도메인 입니다.");
    }

    public static RuntimeException notFoundAuthCode() {
        return new MailInvalidException("인증 코드가 존재하지 않습니다.");
    }
}
