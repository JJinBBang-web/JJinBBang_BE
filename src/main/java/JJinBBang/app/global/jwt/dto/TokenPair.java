package JJinBBang.app.global.jwt.dto;

public record TokenPair(
	String accessToken,
	String refreshToken
) {
}
