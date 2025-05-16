package JJinBBang.app.domain.user.dto;

import JJinBBang.app.domain.user.entity.Users;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponseDto {
    private Long id;
    private String email;
    private String university;
    private String univAuthentication;
    
    public static UserInfoResponseDto of(Users user) {

        // university null 처리
        String university = (user.getUniversity() != null) ? user.getUniversity().getUniversityName() : "";

        return UserInfoResponseDto.builder()
                .id(user.getUserId())
                .email(user.getUniversityEmail())
                .university(university)
                .univAuthentication(user.getVerificationStatus().getStatus())
                .build();
    }
}