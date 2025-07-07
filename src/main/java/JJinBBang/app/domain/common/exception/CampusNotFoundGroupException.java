package JJinBBang.app.domain.common.exception;

import JJinBBang.app.global.error.exception.NotFoundGroupException;

public class CampusNotFoundGroupException extends NotFoundGroupException {
    public CampusNotFoundGroupException(String message) {
        super(message);
    }
}
