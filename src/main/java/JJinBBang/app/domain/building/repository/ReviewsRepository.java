package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.domain.building.enums.ReviewType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface ReviewsRepository extends CrudRepository<Reviews,Long> {
    Reviews findFirstByBuildingOrderByCreatedAtDesc(Buildings building);
    Reviews findFirstByAgencyAndDtypeOrderByCreatedAtDesc(Agencies agency, ReviewType dtype);
    Page<Reviews> findAllByBuilding(Buildings building, Pageable pageable);
    Page<Reviews> findAllByAgency(Agencies agency, Pageable pageable);
}
