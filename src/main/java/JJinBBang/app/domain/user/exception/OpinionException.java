package JJinBBang.app.domain.user.exception;


import JJinBBang.app.global.error.exception.InvalidGroupException;

public class OpinionException extends InvalidGroupException {
    public OpinionException(String message) {
        super(message);
    }

    public static OpinionException NoOptionException() {
        return new OpinionException("선택된 옵션이 없습니다.");
    }

    public static OpinionException OptionMappingException() {
        return new OpinionException("해당하는 옵션의 문항이 존재하지 않습니다.");
    }
}
