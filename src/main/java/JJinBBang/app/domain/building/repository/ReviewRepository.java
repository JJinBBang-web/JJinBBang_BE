package JJinBBang.app.domain.building.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.entity.Reviews;

@Repository
public interface ReviewRepository extends JpaRepository<Reviews, Long> {
	Page<Reviews> findAllByBuilding(Buildings building, Pageable pageable);
	Page<Reviews> findAllByAgency(Agencies agency, Pageable pageable);
}
