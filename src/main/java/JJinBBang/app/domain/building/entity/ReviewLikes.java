package JJinBBang.app.domain.building.entity;

import JJinBBang.app.domain.user.entity.Users;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewLikes {
    @EmbeddedId
    private ReviewLikeId id;

    @MapsId("reviewId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Reviews review;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    public static ReviewLikes create(Reviews review, Users user) {
        ReviewLikeId reviewId = ReviewLikeId.of(review.getId(),user.getUserId());
        ReviewLikes reviewLikes = new ReviewLikes();
        reviewLikes.id = reviewId;
        reviewLikes.review = review;
        reviewLikes.user = user;
        return reviewLikes;
    }
}