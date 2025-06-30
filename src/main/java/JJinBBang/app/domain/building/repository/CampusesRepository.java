package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.common.entity.Campuses;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CampusesRepository extends JpaRepository<Campuses, Long> {
    Optional<Campuses> findByCampusName(String campusName);
}
