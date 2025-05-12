package JJinBBang.app.domain.user.exception;

import JJinBBang.app.global.error.exception.AuthGroupException;

public class UserAuthException extends AuthGroupException {
    public UserAuthException(String message) {
        super(message);
    }

    public static UserAuthException alreadyExists(){
        return new UserAuthException("이미 회원가입된 유저입니다.");
    }
}
