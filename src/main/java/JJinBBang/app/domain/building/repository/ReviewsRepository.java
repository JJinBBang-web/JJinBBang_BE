package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.domain.building.enums.BuildingType;
import JJinBBang.app.domain.building.enums.ReviewType;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface ReviewsRepository extends CrudRepository<Reviews,Long> {
    Reviews findFirstByBuildingOrderByCreatedAtDesc(Buildings building);
    Reviews findFirstByAgencyAndDtypeOrderByCreatedAtDesc(Agencies agency, ReviewType dtype);
    Page<Reviews> findAllByBuilding(Buildings building, Pageable pageable);
    List<Reviews> findAllByBuilding(Buildings building);
    Page<Reviews> findAllByAgency(Agencies agency, Pageable pageable);

    @Query("SELECT DISTINCT r.buildingType FROM Reviews r WHERE r.building = :building")
    Set<BuildingType> findDistinctBuildingTypesByBuilding(Buildings building);
}
