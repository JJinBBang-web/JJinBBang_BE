package JJinBBang.app.global.error.exception;

public abstract class InternalServerErrorGroupException extends RuntimeException {
    public InternalServerErrorGroupException(String message) {
        super(message);
    }
}
