package JJinBBang.app.domain.building.entity;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AgencyLikeId implements Serializable {
    private Long agencyId;
    private Long userId;

    public static AgencyLikeId of(Long agencyId, Long userId) {
        if (agencyId == null || userId == null) {
            throw new IllegalArgumentException("agencyId와 UserId는 null이 될 수 없습니다.");
        }
        return new AgencyLikeId(agencyId, userId);
    }
}