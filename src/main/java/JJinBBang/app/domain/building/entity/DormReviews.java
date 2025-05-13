package JJinBBang.app.domain.building.entity;

import JJinBBang.app.domain.building.enums.Floor;
import jakarta.persistence.*;

@Entity
@Table(name = "dorm_reviews")
@PrimaryKeyJoinColumn(name = "review_id")
@DiscriminatorValue("DORM")
public class DormReviews extends Reviews {

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Floor floor;

	@Column(nullable = false)
	private Integer capacity;

	@Column(name = "dorm_fee", nullable = false)
	private Integer dormFee;

	@Column(name = "current_region", length = 255)
	private String currentRegion;

	@Column(name = "current_grade", nullable = true)
	private Double currentGrade;
}
