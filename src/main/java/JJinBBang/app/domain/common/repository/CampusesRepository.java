package JJinBBang.app.domain.common.repository;

import JJinBBang.app.domain.common.entity.Campuses;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampusesRepository extends JpaRepository<Campuses, Long> {
    List<Campuses> findByUniversityId(Long universityId);
}
