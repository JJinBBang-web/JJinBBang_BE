package JJinBBang.app.domain.building.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class DormFacilityId implements Serializable {
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "facility_id")
    private Long facilityId;

    public static DormFacilityId of(Long reviewId, Long facilityId) {
        if (reviewId == null || facilityId == null) {
            throw new IllegalArgumentException("ReviewId facilityId는 null이 될 수 없습니다.");
        }
        return new DormFacilityId(reviewId, facilityId);
    }
}
