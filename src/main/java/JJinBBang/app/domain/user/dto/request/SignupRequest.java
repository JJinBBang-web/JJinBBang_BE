package JJinBBang.app.domain.user.dto.request;

public record SignupRequest(
        String oauthProvider,
        String oauthCode
) {
}