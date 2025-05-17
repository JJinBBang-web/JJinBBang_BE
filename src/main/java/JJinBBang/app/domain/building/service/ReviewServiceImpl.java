package JJinBBang.app.domain.building.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import JJinBBang.app.domain.building.dto.ReviewDetailResponse;
import JJinBBang.app.domain.building.entity.AgencyReviews;
import JJinBBang.app.domain.building.entity.DormReviews;
import JJinBBang.app.domain.building.entity.GeneralReviews;
import JJinBBang.app.domain.building.entity.ReviewDetails;
import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.domain.building.exception.MissingReviewDetailException;
import JJinBBang.app.domain.building.exception.ReviewNotFoundException;
import JJinBBang.app.domain.building.exception.UnrecognizedReviewTypeException;
import JJinBBang.app.domain.building.repository.ReviewDetailRepository;
import JJinBBang.app.domain.building.repository.ReviewsRepository;
import JJinBBang.app.domain.user.entity.Users;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

	private final ReviewsRepository reviewsRepository;
	private final ReviewDetailRepository reviewDetailRepository;

	@Override
	@Transactional(readOnly = true)
	public ReviewDetailResponse getReviewDetail(Long reviewId, Users user) {
		Reviews review = getReview(reviewId);

		Boolean liked = review.getReviewLikes().stream()
			.anyMatch(like -> like.getUser().equals(user));

		ReviewDetails reviewDetail = reviewDetailRepository.findByReviewId(reviewId).orElseThrow(
			() -> new MissingReviewDetailException(reviewId));

		if (review instanceof GeneralReviews generalReview) {
			return ReviewDetailResponse.ofGeneral(generalReview, reviewDetail, liked);
		} else if (review instanceof DormReviews dormReview) {
			return ReviewDetailResponse.ofDormitory(dormReview, reviewDetail, liked);
		} else if (review instanceof AgencyReviews agencyReview) {
			return ReviewDetailResponse.ofAgency(agencyReview, reviewDetail, liked);
		} else {
			throw new UnrecognizedReviewTypeException();
		}
	}

	private Reviews getReview(Long reviewId) {
		return reviewsRepository.findById(reviewId).orElseThrow(ReviewNotFoundException::new);
	}
}
