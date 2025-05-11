package JJinBBang.app.domain.user.dto.response;

import JJinBBang.app.domain.user.entity.Users;

public record UserInfoResponse(
        Long userId,
        String provider,
        String providerId,
        String universityEmail,
        String verificationStatus
) {
    public static UserInfoResponse from(Users user) {
        return new UserInfoResponse(
                user.getUserId(),
                user.getProvider().toString(),
                user.getProviderId(),
                user.getUniversityEmail(),
                user.getVerificationStatus().toString()
        );
    }
}
