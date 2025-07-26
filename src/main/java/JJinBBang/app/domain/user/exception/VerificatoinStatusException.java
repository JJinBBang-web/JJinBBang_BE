package JJinBBang.app.domain.user.exception;

import JJinBBang.app.global.error.exception.InvalidGroupException;

public class VerificatoinStatusException extends InvalidGroupException {
    public VerificatoinStatusException(String message) {super(message);}

    public static VerificatoinStatusException InvalidVerificationStatusException (){
        return new VerificatoinStatusException("존재하지 않는 인증 상태입니다.");
    }
}