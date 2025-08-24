package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.BuildingLikeId;
import JJinBBang.app.domain.building.entity.BuildingLikes;
import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.user.entity.Users;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface BuildingLikesRepository extends CrudRepository<BuildingLikes, BuildingLikeId> {
    Optional<BuildingLikes> findByBuildingAndUser(Buildings building, Users user);

    Boolean existsByBuildingAndUser(Buildings buildings, Users users);

    @EntityGraph(attributePaths = {"building"})
    List<BuildingLikes> findAllByUser_UserId(Long userId);
}
