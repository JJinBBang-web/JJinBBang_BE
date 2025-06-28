package JJinBBang.app.domain.user.dto.request;

public record VerifyEmailCodeRequest(
        String emailAddress,
        String authCode
) {
}
