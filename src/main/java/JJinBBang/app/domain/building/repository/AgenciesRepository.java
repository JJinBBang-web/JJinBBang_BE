package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.repository.custom.AgenciesRepositoryCustom;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgenciesRepository extends JpaRepository<Agencies, Long>, AgenciesRepositoryCustom {
    Optional<Agencies> findByBuildingCode(String buildingCode);
}
