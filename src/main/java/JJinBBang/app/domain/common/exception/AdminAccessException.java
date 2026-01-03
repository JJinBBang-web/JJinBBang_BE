package JJinBBang.app.domain.common.exception;

import JJinBBang.app.global.error.exception.AccessDeniedGroupException;

public class AdminAccessException extends AccessDeniedGroupException {
    public AdminAccessException(String message) {
        super(message);
    }

    public static AdminAccessException accessDenied() {
        return new AdminAccessException("관리자 외 접근 불가입니다.");
    }
}
