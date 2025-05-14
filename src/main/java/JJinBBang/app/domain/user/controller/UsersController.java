package JJinBBang.app.domain.user.controller;

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
@RequestMapping("/api/v1/user/test")
@RequiredArgsConstructor
public class UsersController {

	private final UsersService usersService;

	@GetMapping("")
	public String test(@AuthenticationPrincipal Users user) {
		System.out.println("user.getProviderId() = " + user.getProviderId());
		return "success";
	}

	@GetMapping("/all")
	public String test() {
		return "success";
	}
}
