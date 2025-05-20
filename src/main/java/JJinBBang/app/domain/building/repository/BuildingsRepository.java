package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.Buildings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BuildingsRepository extends JpaRepository<Buildings, Long> {
    Optional<Buildings> findByBuildingCode(String buildingCode);

}
