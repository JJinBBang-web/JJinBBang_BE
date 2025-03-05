package JJinBBang.app.domain.building.exception;

import JJinBBang.app.global.error.exception.NotFoundGroupException;

public class ReviewsNotFoundException extends NotFoundGroupException {
    public ReviewsNotFoundException(String message) {
        super(message);
    }
}
