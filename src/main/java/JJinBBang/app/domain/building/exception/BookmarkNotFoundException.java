package JJinBBang.app.domain.building.exception;

import JJinBBang.app.global.error.exception.NotFoundGroupException;

public class BookmarkNotFoundException extends NotFoundGroupException {
    public BookmarkNotFoundException(String message) {super(message);}
}
