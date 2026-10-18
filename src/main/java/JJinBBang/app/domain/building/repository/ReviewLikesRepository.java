package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.ReviewLikeId;
import JJinBBang.app.domain.building.entity.ReviewLikes;
import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.domain.user.entity.Users;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReviewLikesRepository extends JpaRepository<ReviewLikes, ReviewLikeId> {
    Optional<ReviewLikes> findByReviewAndUser(Reviews reviews, Users user);
    Optional<ReviewLikes> findByReviewIdAndUserUserId(Long reviewId, Long userId);
    Boolean existsByReviewAndUser(Reviews reviews, Users user);

    @Query("SELECT rl.review.id FROM ReviewLikes rl WHERE rl.user.userId = :userId AND rl.review.id IN :reviewIds")
    Set<Long> findLikedReviewIds(@Param("userId") Long userId, @Param("reviewIds") Collection<Long> reviewIds);


	@EntityGraph(attributePaths = {"review"})
	List<ReviewLikes> findAllByUser_UserId(Long targetUserId);
}
