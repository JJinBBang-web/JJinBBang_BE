package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.BuildingLikeId;
import JJinBBang.app.domain.building.entity.BuildingLikes;
import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.user.entity.Users;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface BuildingLikesRepository extends CrudRepository<BuildingLikes, BuildingLikeId> {
    Optional<BuildingLikes> findByBuildingAndUser(Buildings building, Users user);
}
