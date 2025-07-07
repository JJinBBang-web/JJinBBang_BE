package JJinBBang.app.domain.building.entity;

import JJinBBang.app.domain.building.enums.UsageType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dormitory_facilities")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DormitoryFacilities {

    @EmbeddedId
    private DormFacilityId id;

    // PK(review_id) → Reviews 엔티티와 N:1
    @MapsId("reviewId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id")
    private DormReviews dormitoryReview;

    // PK(facility_id) → Facility 엔티티와 N:1
    @MapsId("facilityId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facility_id")
    private Facilities facility;

    @Column(name = "available", nullable = false)
    private Boolean available;      // TINYINT(1) → Boolean

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_type", length = 7)
    private UsageType usageType;    // ENUM('private','public')

    public static DormitoryFacilities create(DormReviews review, Facilities facility, Boolean isAvailable, UsageType usageType) {
        DormFacilityId dormFacilityId = DormFacilityId.of(review.getId(),facility.getFacilityId());
        DormitoryFacilities dormitoryFacility = new DormitoryFacilities();
        dormitoryFacility.id = dormFacilityId;
        dormitoryFacility.dormitoryReview = review;
        dormitoryFacility.facility = facility;
        dormitoryFacility.available = isAvailable;
        dormitoryFacility.usageType = usageType;

        return dormitoryFacility;
    }
}
