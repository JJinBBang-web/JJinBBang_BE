package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.AgencyReviews;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgencyReviewsRepository extends JpaRepository<AgencyReviews, Long> {

    @EntityGraph(attributePaths = "agency")
    Page<AgencyReviews> findByUser_UserId(Long userId, Pageable pageable);
}
