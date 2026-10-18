package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.BuildingLikeId;
import JJinBBang.app.domain.building.entity.BuildingLikes;
import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.user.entity.Users;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BuildingLikesRepository extends CrudRepository<BuildingLikes, BuildingLikeId> {
    Optional<BuildingLikes> findByBuildingAndUser(Buildings building, Users user);

    Boolean existsByBuildingAndUser(Buildings buildings, Users users);

    @Query("SELECT bl.building.id FROM BuildingLikes bl WHERE bl.user.userId = :userId AND bl.building.id IN :buildingIds")
    Set<Long> findLikedBuildingIds(@Param("userId") Long userId, @Param("buildingIds") Collection<Long> buildingIds);

    @EntityGraph(attributePaths = {"building"})
    List<BuildingLikes> findAllByUser_UserId(Long userId);
}
