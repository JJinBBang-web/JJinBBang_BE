package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.GeneralReviews;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneralReviewsRepository extends JpaRepository<GeneralReviews, Long> {

    @EntityGraph(attributePaths = "building")
    Page<GeneralReviews> findByUser_UserId(Long userId, Pageable pageable);
}