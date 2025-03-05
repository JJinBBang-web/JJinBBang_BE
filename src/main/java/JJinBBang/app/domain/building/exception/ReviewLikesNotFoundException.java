package JJinBBang.app.domain.building.exception;

import JJinBBang.app.global.error.exception.NotFoundGroupException;

public class ReviewLikesNotFoundException extends NotFoundGroupException {
    public ReviewLikesNotFoundException(String message) {
        super(message);
    }
}
