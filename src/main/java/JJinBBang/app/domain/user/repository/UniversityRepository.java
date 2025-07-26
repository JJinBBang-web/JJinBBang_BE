package JJinBBang.app.domain.user.repository;

import JJinBBang.app.domain.common.entity.Universities;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UniversityRepository extends JpaRepository<Universities, Long> {

    @Override
    @EntityGraph(attributePaths = {"campuses"})
    Page<Universities> findAll(Pageable pageable);

    Optional<Universities> findUniversitiesByUniversityDomain(String universityDomain);
    boolean existsByUniversityDomain(String universityDomain);
}
