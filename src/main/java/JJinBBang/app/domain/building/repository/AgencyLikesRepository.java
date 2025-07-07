package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.AgencyLikes;
import JJinBBang.app.domain.user.entity.Users;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AgencyLikesRepository extends CrudRepository<AgencyLikes, Long> {
    Optional<AgencyLikes> findByAgencyAndUser(Agencies agency, Users user);

}
