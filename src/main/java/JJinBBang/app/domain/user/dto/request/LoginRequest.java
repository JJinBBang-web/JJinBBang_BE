package JJinBBang.app.domain.user.dto.request;

public record LoginRequest(
        String oauthProvider,
        String oauthAccessToken
) {
}
