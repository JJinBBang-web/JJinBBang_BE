package JJinBBang.app.domain.common.repository;

import JJinBBang.app.domain.common.entity.Universities;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UniversitiesRepository extends JpaRepository<Universities, Long> {

    Optional<Universities> findByUniversityName(String universityName);
}
