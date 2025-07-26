package JJinBBang.app.domain.user.dto.request;

public record UpdateVerificationStatusDto(
        Long userId,
        String verificationStatus
) {
}