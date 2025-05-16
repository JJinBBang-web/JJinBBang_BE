package JJinBBang.app.domain.building.exception;

import JJinBBang.app.global.error.exception.NotFoundGroupException;

public class BookmarkNotFoundException extends NotFoundGroupException {
    public BookmarkNotFoundException(String message) {super("해당 "+message+"이 존재하지 않습니다.");}
}
