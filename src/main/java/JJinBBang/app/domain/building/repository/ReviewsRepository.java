package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.domain.building.enums.ReviewType;
import org.springframework.data.repository.CrudRepository;

public interface ReviewsRepository extends CrudRepository<Reviews,Long> {
    Reviews findFirstByBuildingOrderByCreatedAtDesc(Buildings building);
    Reviews findFirstByAgencyAndDtypeOrderByCreatedAtDesc(Agencies agency, ReviewType dtype);
}
