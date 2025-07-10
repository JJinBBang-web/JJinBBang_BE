package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.common.entity.Campuses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("BuildingCampusesRepository")
public interface CampusesRepository extends JpaRepository<Campuses, Long> {
    Optional<Campuses> findByCampusName(String campusName);
}
