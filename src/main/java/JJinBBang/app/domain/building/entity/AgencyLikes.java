package JJinBBang.app.domain.building.entity;

import JJinBBang.app.domain.user.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgencyLikes {
    @EmbeddedId
    private AgencyLikeId id;

    @MapsId("agencyId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agencies agency;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    public static AgencyLikes create(Agencies agency, Users user) {
        AgencyLikeId agencyLikeId = AgencyLikeId.of(agency.getAgencyId(),user.getUserId());
        AgencyLikes agencyLikes = new AgencyLikes();
        agencyLikes.id = agencyLikeId;
        agencyLikes.agency = agency;
        agencyLikes.user = user;
        return agencyLikes;
    }
}