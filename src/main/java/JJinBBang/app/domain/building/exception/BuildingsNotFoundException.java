package JJinBBang.app.domain.building.exception;

import JJinBBang.app.global.error.exception.NotFoundGroupException;

public class BuildingsNotFoundException extends NotFoundGroupException {
    public BuildingsNotFoundException(String message) {
        super(message);
    }
}
