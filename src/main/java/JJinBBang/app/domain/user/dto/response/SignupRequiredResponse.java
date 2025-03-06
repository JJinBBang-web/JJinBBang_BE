package JJinBBang.app.domain.user.dto.response;

public record SignupRequiredResponse (
        String signupToken
) {
    public static SignupRequiredResponse of(String signupToken) {
        return new SignupRequiredResponse(signupToken);
    }
}
