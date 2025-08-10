package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.common.entity.Campuses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("BuildingCampusesRepository")
public interface CampusesRepository extends JpaRepository<Campuses, Long> {
}
