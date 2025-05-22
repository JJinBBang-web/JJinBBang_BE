package JJinBBang.app.domain.building.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import JJinBBang.app.domain.building.dto.ReviewDetailResponse;
import JJinBBang.app.domain.building.service.ReviewService;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
public class ReviewController {
	private final ReviewService reviewService;

	@GetMapping("/{reviewId}")
	public ResTemplate<ReviewDetailResponse> getReviewDetail(@PathVariable("reviewId") Long reviewId, @AuthenticationPrincipal Users user) {
		ReviewDetailResponse data = reviewService.getReviewDetail(reviewId, user);
		return new ResTemplate<>(HttpStatus.OK, "리뷰 불러오기 성공", data);
	}
}
