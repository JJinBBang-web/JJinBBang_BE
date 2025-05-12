package JJinBBang.app.domain.building.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import JJinBBang.app.domain.building.entity.Buildings;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildingRepository extends JpaRepository<Buildings, Long> {
}
