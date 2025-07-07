package JJinBBang.app.domain.user.dto.response;

public record ReissueAccessTokenResponse(
        String accessToken
) {
    public static ReissueAccessTokenResponse of(String accessToken) {
        return new ReissueAccessTokenResponse(accessToken);
    }
}
