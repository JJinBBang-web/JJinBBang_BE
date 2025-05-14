package JJinBBang.app.domain.user.controller;

import JJinBBang.app.domain.user.dto.UserInfoResponseDto;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.exception.InvalidTokenException;
import JJinBBang.app.domain.user.service.UserInfoService;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserInfoController {

    private final UserInfoService userInfoService;

    @GetMapping
    public ResTemplate<UserInfoResponseDto> getUserInfo(
            @AuthenticationPrincipal Users user
    ) {
        if (user == null) {
            throw InvalidTokenException.unauthorized(); // 401
        }

        UserInfoResponseDto userInfo = userInfoService.getUserInfo(user);

        return new ResTemplate<>(
                HttpStatus.OK,
                "유저 정보조회 성공",
                userInfo
        );
    }
}
