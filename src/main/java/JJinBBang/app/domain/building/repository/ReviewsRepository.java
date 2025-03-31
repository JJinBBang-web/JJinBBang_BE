package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.Reviews;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ReviewsRepository extends CrudRepository<Reviews,Long> {
    List<Reviews> findByIdIn(List<Long> reveiwIds);
}
