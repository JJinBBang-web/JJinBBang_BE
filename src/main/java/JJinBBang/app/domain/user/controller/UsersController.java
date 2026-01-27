package JJinBBang.app.domain.user.controller;

import JJinBBang.app.domain.building.dto.UserReviewListResponse;
import JJinBBang.app.domain.user.dto.UserInfoResponseDto;
import JJinBBang.app.domain.user.dto.request.UnregisterReasonRequest;
import JJinBBang.app.domain.user.dto.request.UserOpinionRequest;
import JJinBBang.app.domain.user.exception.InvalidTokenException;
import JJinBBang.app.global.sheets.dto.UnregisterReasonDto;
import JJinBBang.app.global.sheets.dto.UserOpinionDto;
import JJinBBang.app.global.sheets.service.GoogleSheetsService;
import JJinBBang.app.global.slack.service.SlackService;
import JJinBBang.app.global.template.ResTemplate;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.service.UsersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UsersController {

	private final UsersService usersService;
	private final GoogleSheetsService googleSheetsService;
	private final SlackService slackService;

	@GetMapping
	public ResTemplate<UserInfoResponseDto> getUserInfo(@AuthenticationPrincipal Users user) {
		if (user == null) {
			throw InvalidTokenException.unauthorized();
		}
		log.info("유저 조회 성공 : {}", user.getUserId());
		Users withUniversity =  usersService.findWithUniversity(user.getProviderId());

		UserInfoResponseDto response = usersService.getUserInfo(withUniversity);
		return new ResTemplate<>(HttpStatus.OK, "유저 정보조회 성공", response);
	}

	@GetMapping("/review")
	public ResTemplate<UserReviewListResponse> getUserReview(
			@AuthenticationPrincipal Users user,
			@RequestParam(defaultValue = "0") int offset,
			@RequestParam(defaultValue = "10") int limit,
			@RequestParam(defaultValue = "latest") String orderby
	) {
		if (user == null) {
			throw InvalidTokenException.unauthorized();
		}
		var reviews = usersService.getUserReviews(user, offset, limit, orderby);
		return new ResTemplate<>(HttpStatus.OK, "리뷰 조회 성공", reviews);
	}

	@PostMapping("/unregisterReason")
	public ResTemplate<?> addUnregisterReason(
			@AuthenticationPrincipal Users user,
			@Valid @RequestBody UnregisterReasonRequest unregisterReasonRequest
	) throws IOException {

		if (user == null) throw InvalidTokenException.unauthorized();

		Long userId = user.getUserId();
		String unregisterReason = usersService.optionToText(unregisterReasonRequest.option());

		UnregisterReasonDto data = new UnregisterReasonDto(
				userId,
				unregisterReasonRequest.option(),
				unregisterReason,
				user.getCreatedAt(),
				LocalDateTime.now()
		);

		googleSheetsService.appendUnregisterReason(data);

		return new ResTemplate<>(HttpStatus.OK, "탈퇴사유 추가 성공", null);
	}

	@PostMapping("/getOpinion")
	public ResTemplate<?> addUserOpinion(
			@AuthenticationPrincipal Users user,
			@Valid @RequestBody UserOpinionRequest userOpinionRequest
	) throws IOException {

		if (user == null) throw InvalidTokenException.unauthorized();

		Long userId = user.getUserId();

		UserOpinionDto data = new UserOpinionDto(
				userId,
				userOpinionRequest.targetId(),
				userOpinionRequest.opinion(),
				LocalDateTime.now()
		);

		googleSheetsService.appendUserOpinion(data, userOpinionRequest.opinionType());

		slackService.sendOpinionMessage(
				userId,
				"\n" +
						userOpinionRequest.opinion() +
						"\n\n대상: " + userOpinionRequest.targetId() +
						"\n유형: " + userOpinionRequest.opinionType()
		);

		return new ResTemplate<>(HttpStatus.OK, "문의 성공", null);
	}
}
