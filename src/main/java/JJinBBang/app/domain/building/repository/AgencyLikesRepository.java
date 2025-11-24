package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.AgencyLikeId;
import JJinBBang.app.domain.building.entity.AgencyLikes;
import JJinBBang.app.domain.user.entity.Users;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgencyLikesRepository extends JpaRepository<AgencyLikes, AgencyLikeId> {
    Optional<AgencyLikes> findByAgencyAndUser(Agencies agency, Users user);

    Boolean existsByAgencyAndUser(Agencies agencies, Users users);

	@EntityGraph(attributePaths = {"agency"})
	List<AgencyLikes> findAllByUser_UserId(Long targetUserId);
}
