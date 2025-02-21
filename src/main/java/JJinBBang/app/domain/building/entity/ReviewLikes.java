package JJinBBang.app.domain.building.entity;

import JJinBBang.app.domain.user.entity.Users;
import jakarta.persistence.*;
        import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "review_likes")
// @IdClass(ReviewLikeId.class)
public class ReviewLikes {

    @Id
    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    private Reviews review;

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
}
