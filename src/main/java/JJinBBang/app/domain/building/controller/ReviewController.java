package JJinBBang.app.domain.building.controller;

import io.micrometer.core.annotation.Timed;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import JJinBBang.app.domain.building.dto.CreateReviewResponse;
import JJinBBang.app.domain.building.dto.ReviewDetailResponse;
import JJinBBang.app.domain.building.dto.ReviewRequest;
import JJinBBang.app.domain.building.enums.ReviewType;
import JJinBBang.app.domain.building.service.ReviewService;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
@Timed(value = "jjinbbang.api.review", extraTags = {"module", "review"})
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/{reviewId}")
    public ResTemplate<ReviewDetailResponse> getReviewDetail(@PathVariable("reviewId") Long reviewId, @AuthenticationPrincipal Users user) {
        ReviewDetailResponse data = reviewService.getReviewDetail(reviewId, user);
        return new ResTemplate<>(HttpStatus.OK, "리뷰 불러오기 성공", data);
    }

    @PostMapping("/{reviewType}")
    public ResTemplate<CreateReviewResponse> createReview(
            @AuthenticationPrincipal Users user,
            @Validated @RequestBody ReviewRequest reviewRequest,
            @PathVariable ReviewType reviewType) {

        CreateReviewResponse data = reviewService.createReview(reviewRequest, user, reviewType);
        return new ResTemplate<>(HttpStatus.OK, "리뷰 작성 성공", data);
    }

    @PutMapping("/{reviewId}")
    public ResTemplate<Void> updateReview(
            @AuthenticationPrincipal Users user,
            @Validated @RequestBody ReviewRequest reviewRequest,
            @PathVariable Long reviewId) {

        reviewService.updateReview(reviewRequest, user, reviewId);
        return new ResTemplate<>(HttpStatus.OK, "리뷰 수정 성공", null);
    }

    @DeleteMapping("/{reviewId}")
    public ResTemplate<Void> deleteReview(
            @AuthenticationPrincipal Users user,
            @PathVariable Long reviewId) {
        reviewService.deleteReview(user, reviewId);
        return new ResTemplate<>(HttpStatus.OK, "리뷰 삭제 성공", null);
    }
}
