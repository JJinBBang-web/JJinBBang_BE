package JJinBBang.app.domain.user.exception;

import JJinBBang.app.global.error.exception.NotFoundGroupException;

public class AuthNotFoundException extends NotFoundGroupException {
    public AuthNotFoundException(String message) {
        super(message);
    }

    public static AuthNotFoundException socialProviderNotFound(){
        return new AuthNotFoundException("지원되지 않는 소셜 타입입니다.");
    }
}
