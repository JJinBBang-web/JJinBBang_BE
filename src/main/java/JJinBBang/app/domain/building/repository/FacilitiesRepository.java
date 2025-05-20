package JJinBBang.app.domain.building.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import JJinBBang.app.domain.building.entity.Facilities;

@Repository
public interface FacilitiesRepository extends JpaRepository<Facilities, Long> {
	Optional<Facilities> findByName(String name);
}
