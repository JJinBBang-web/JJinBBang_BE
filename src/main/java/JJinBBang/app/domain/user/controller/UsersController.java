package JJinBBang.app.domain.user.controller;

import JJinBBang.app.domain.user.dto.UserInfoResponseDto;
import JJinBBang.app.domain.user.dto.response.UserInfoResponse;
import JJinBBang.app.domain.user.exception.InvalidTokenException;
import JJinBBang.app.global.template.ResTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.service.UsersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UsersController {

	private final UsersService usersService;

	@GetMapping
	public ResTemplate<UserInfoResponseDto> getUserInfo(@AuthenticationPrincipal Users user) {
		if (user == null) {
			throw InvalidTokenException.unauthorized();
		}
		log.info("유저 조회 성공 : {}", user.getUserId());

		UserInfoResponseDto response = usersService.getUserInfo(user);
		return new ResTemplate<>(HttpStatus.OK, "유저 정보조회 성공", response);
	}
}
