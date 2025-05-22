package JJinBBang.app.domain.user.dto.response;

import JJinBBang.app.domain.user.entity.Users;
import lombok.Builder;

@Builder
public record CertificateUserInfoResponse(
        Long userId,
        Long universityId,
        String universityName
) {
    public static CertificateUserInfoResponse of(Users user) {
        return CertificateUserInfoResponse.builder()
                .userId(user.getUserId())
                .universityId(user.getUniversity().getId())
                .universityName(user.getUniversity().getUniversityName())
                .build();
    }
}
