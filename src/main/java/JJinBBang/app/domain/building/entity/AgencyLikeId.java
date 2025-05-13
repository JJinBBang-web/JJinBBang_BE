package JJinBBang.app.domain.building.entity;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

@Embeddable
@EqualsAndHashCode
public class AgencyLikeId implements Serializable {
    private Long agencyId;
    private Long userId;
}