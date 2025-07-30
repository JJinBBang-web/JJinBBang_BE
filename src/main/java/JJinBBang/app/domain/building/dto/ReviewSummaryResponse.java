package JJinBBang.app.domain.building.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import JJinBBang.app.domain.building.entity.AgencyReviews;
import JJinBBang.app.domain.building.entity.DormReviews;
import JJinBBang.app.domain.building.entity.GeneralReviews;
import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.domain.building.exception.ReviewInternalException;
import JJinBBang.app.global.common.dto.AgencyReviewInfo;
import JJinBBang.app.global.common.dto.DormitoryReviewInfo;
import JJinBBang.app.global.common.dto.GeneralReviewInfo;
import JJinBBang.app.global.common.dto.ReviewInfo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewSummaryResponse {
		@JsonInclude(JsonInclude.Include.NON_NULL)
		private final GeneralReviewInfo generalReviewInfo;

		@JsonInclude(JsonInclude.Include.NON_NULL)
		private final DormitoryReviewInfo dormitoryReviewInfo;

		@JsonInclude(JsonInclude.Include.NON_NULL)
		private final AgencyReviewInfo agencyReviewInfo;

		private final ReviewInfo reviewInfo;
		private final String image;

		public static ReviewSummaryResponse of(Reviews review, Boolean like) {
			GeneralReviewInfo generalReviewInfo = null;
			DormitoryReviewInfo dormitoryReviewInfo = null;
			AgencyReviewInfo agencyReviewInfo = null;

			if (review instanceof GeneralReviews generalReview) {
				generalReviewInfo = GeneralReviewInfo.of(generalReview, like);
			} else if (review instanceof DormReviews dormitoryReview) {
				dormitoryReviewInfo = DormitoryReviewInfo.of(dormitoryReview, like);
			} else if (review instanceof AgencyReviews agencyReview) {
				agencyReviewInfo = AgencyReviewInfo.of(agencyReview, like);
			} else {
				throw new ReviewInternalException();
			}

			return ReviewSummaryResponse.builder()
				.generalReviewInfo(generalReviewInfo)
				.dormitoryReviewInfo(dormitoryReviewInfo)
				.agencyReviewInfo(agencyReviewInfo)
				.reviewInfo(ReviewInfo.of(review))
				.image(review.getThumbnailImage())
				.build();
		}
}
