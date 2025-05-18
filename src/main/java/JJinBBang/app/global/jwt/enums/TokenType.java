package JJinBBang.app.global.jwt.enums;

public enum TokenType {
    ACCESS("access"),
    REFRESH("refresh"),
    SIGNUP("signup");

    private final String type;

    TokenType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
