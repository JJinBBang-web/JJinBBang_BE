package JJinBBang.app.domain.user.exception;

import JJinBBang.app.global.error.exception.AuthGroupException;

public class InvalidTokenException extends AuthGroupException {
    public InvalidTokenException(String message) {
        super(message);
    }

    public static InvalidTokenException unauthorized() {
        return new InvalidTokenException("로그인이 필요합니다.");
    }
}
