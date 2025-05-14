package JJinBBang.app.domain.building.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "facilities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Facility {

    @Id
    @Column(name = "facility_id")
    private Long facilityId;                    // 편의시설 종류 고유 식별자

    @Column(name = "name", nullable = false, length = 100)
    private String name;                        // 편의시설 이름

    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DormFacility> dormFacilities = new ArrayList<>();
}
