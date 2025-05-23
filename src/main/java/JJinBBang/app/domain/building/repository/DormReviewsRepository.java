package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.DormReviews;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DormReviewsRepository extends JpaRepository<DormReviews, Long> {

    @EntityGraph(attributePaths = {"building", "building.campus"})
    Page<DormReviews> findByUser_UserId(Long userId, Pageable pageable);
}