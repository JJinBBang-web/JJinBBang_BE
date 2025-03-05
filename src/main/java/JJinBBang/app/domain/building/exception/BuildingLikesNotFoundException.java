package JJinBBang.app.domain.building.exception;

import JJinBBang.app.global.error.exception.NotFoundGroupException;

public class BuildingLikesNotFoundException extends NotFoundGroupException {
    public BuildingLikesNotFoundException(String message) {
        super(message);
    }
}
