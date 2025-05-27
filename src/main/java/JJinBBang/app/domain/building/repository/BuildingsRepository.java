package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.Buildings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BuildingsRepository extends JpaRepository<Buildings, Long>, BuildingRepositoryCustom {
    Optional<Buildings> findByBuildingCode(String buildingCode);

}
