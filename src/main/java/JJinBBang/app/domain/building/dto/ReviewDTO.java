package JJinBBang.app.domain.building.dto;

import JJinBBang.app.domain.building.entity.Reviews;
import lombok.Getter;

@Getter
public class ReviewDTO {
    private Long reviewId;
    private String content;
    private Double reviewRating;

    public ReviewDTO(Reviews review) {
        this.reviewId = review.getId();
        this.content = review.getContent();
        this.reviewRating = review.getReviewRating();
    }
}

