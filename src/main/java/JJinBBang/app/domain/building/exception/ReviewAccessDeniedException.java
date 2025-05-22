package JJinBBang.app.domain.building.exception;

import JJinBBang.app.global.error.exception.AccessDeniedGroupException;

public class ReviewAccessDeniedException extends AccessDeniedGroupException {
    public ReviewAccessDeniedException(String message) {
        super(message);
    }

    public static ReviewAccessDeniedException onlyAuthorCanEdit() {
        return new ReviewAccessDeniedException("작성자만 수정할 수 있습니다.");
    }

    public static ReviewAccessDeniedException onlyAuthorCanDelete() {
        return new ReviewAccessDeniedException("작성자만 삭제할 수 있습니다.");
    }
}
