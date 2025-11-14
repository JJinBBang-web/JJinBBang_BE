package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.ReviewLikeId;
import JJinBBang.app.domain.building.entity.ReviewLikes;
import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.domain.user.entity.Users;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewLikesRepository extends JpaRepository<ReviewLikes, ReviewLikeId> {
    Optional<ReviewLikes> findByReviewAndUser(Reviews reviews, Users user);
    Optional<ReviewLikes> findByReviewIdAndUserUserId(Long reviewId, Long userId);
    Boolean existsByReviewAndUser(Reviews reviews, Users user);


	@EntityGraph(attributePaths = {"review"})
	List<ReviewLikes> findAllByUser_UserId(Long targetUserId);
}
