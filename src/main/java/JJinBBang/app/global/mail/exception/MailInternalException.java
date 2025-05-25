package JJinBBang.app.global.mail.exception;

import JJinBBang.app.global.error.exception.InternalServerErrorGroupException;

public class MailInternalException extends InternalServerErrorGroupException {
    public MailInternalException(String message) {
        super(message);
    }

    public static MailInternalException sendFail(){
        return new MailInternalException("메일 전송에 실패하였습니다.");
    }
}
