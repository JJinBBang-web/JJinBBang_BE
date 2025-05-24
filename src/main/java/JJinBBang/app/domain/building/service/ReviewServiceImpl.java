package JJinBBang.app.domain.building.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import JJinBBang.app.domain.building.dto.PageRequest;
import JJinBBang.app.domain.building.dto.ReviewSummaryResponse;
import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.dto.ReviewDetailResponse;
import JJinBBang.app.domain.building.entity.AgencyReviews;
import JJinBBang.app.domain.building.entity.DormReviews;
import JJinBBang.app.domain.building.entity.GeneralReviews;
import JJinBBang.app.domain.building.entity.ReviewDetails;
import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.domain.building.exception.BuildingNullException;
import JJinBBang.app.domain.building.exception.ReviewDetailInternalException;
import JJinBBang.app.domain.building.repository.AgencyRepository;
import JJinBBang.app.domain.building.repository.BuildingRepository;
import JJinBBang.app.domain.building.exception.MissingReviewDetailException;
import JJinBBang.app.domain.building.exception.ReviewNotFoundException;
import JJinBBang.app.domain.building.exception.UnrecognizedReviewTypeException;
import JJinBBang.app.domain.building.repository.ReviewDetailRepository;
import JJinBBang.app.domain.building.repository.ReviewRepository;
import JJinBBang.app.domain.common.dto.PaginatedResponse;
import JJinBBang.app.domain.building.repository.ReviewsRepository;
import JJinBBang.app.domain.user.entity.Users;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

	private final ReviewRepository reviewRepository;
    private final ReviewsRepository reviewsRepository;
	private final ReviewDetailRepository reviewDetailRepository;
	private final AgencyRepository agencyRepository;
	private final BuildingRepository buildingRepository;

	@Override
	@Transactional(readOnly = true)
	public PaginatedResponse<ReviewSummaryResponse> getReviewList(Long buildingId, Boolean isAgency, Users user, PageRequest pageRequest) {

		Page<Reviews> reviewPage;
		if(isAgency) {
			Agencies agency = agencyRepository.findById(buildingId).orElseThrow(BuildingNullException::new);
			reviewPage = reviewRepository.findAllByAgency(agency, pageRequest);
        } else {
            Buildings building = buildingRepository.findById(buildingId).orElseThrow(BuildingNullException::new);
            reviewPage = reviewRepository.findAllByBuilding(building, pageRequest);
		}

		return PaginatedResponse.of(
			reviewPage,
			(review -> {
				Boolean liked = review.getReviewLikes().stream().anyMatch(reviewLike -> reviewLike.getUser().equals(user));
                ReviewDetails reviewDetail = reviewDetailRepository.findByReviewId(review.getId()).orElseThrow(() -> new ReviewDetailInternalException(review.getId()));
				return ReviewSummaryResponse.of(review, liked, reviewDetail);
			})
		);
	}

    @Override
    @Transactional(readOnly = true)
    public ReviewDetailResponse getReviewDetail(Long reviewId, Users user) {
        Reviews review = reviewsRepository.findById(reviewId).orElseThrow(ReviewNotFoundException::new);

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
}
