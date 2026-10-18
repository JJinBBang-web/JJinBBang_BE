package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.AgencyLikeId;
import JJinBBang.app.domain.building.entity.AgencyLikes;
import JJinBBang.app.domain.user.entity.Users;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AgencyLikesRepository extends JpaRepository<AgencyLikes, AgencyLikeId> {
    Optional<AgencyLikes> findByAgencyAndUser(Agencies agency, Users user);

    Boolean existsByAgencyAndUser(Agencies agencies, Users users);

    @Query("SELECT al.agency.agencyId FROM AgencyLikes al WHERE al.user.userId = :userId AND al.agency.agencyId IN :agencyIds")
    Set<Long> findLikedAgencyIds(@Param("userId") Long userId, @Param("agencyIds") Collection<Long> agencyIds);

	@EntityGraph(attributePaths = {"agency"})
	List<AgencyLikes> findAllByUser_UserId(Long targetUserId);
}
