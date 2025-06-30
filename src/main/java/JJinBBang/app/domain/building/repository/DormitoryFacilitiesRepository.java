package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.DormReviews;
import JJinBBang.app.domain.building.entity.DormitoryFacilities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DormitoryFacilitiesRepository extends JpaRepository<DormitoryFacilities, Long> {
    void deleteAllByDormitoryReview(DormReviews oldReview);
}
