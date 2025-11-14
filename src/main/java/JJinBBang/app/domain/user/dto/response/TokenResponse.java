package JJinBBang.app.domain.user.dto.response;

import lombok.Builder;

@Builder
public record TokenResponse(
        String accessToken
) {
    public static TokenResponse of(String accessToken) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}
