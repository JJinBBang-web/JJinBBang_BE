package JJinBBang.app.domain.building.entity;

import JJinBBang.app.domain.building.enums.ContractType;
import JJinBBang.app.domain.building.enums.Floor;
import jakarta.persistence.*;

@Entity
@Table(name = "general_reviews")
@PrimaryKeyJoinColumn(name = "review_id")
@DiscriminatorValue("GENERAL")
public class GeneralReviews extends Reviews {
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Floor floor;

	@Column(nullable = false)
	private Double area;

	@Enumerated(EnumType.STRING)
	@Column(name = "contract_type", nullable = false)
	private ContractType contractType;

	@Column(nullable = true)
	private Integer deposit;

	@Column(nullable = true)
	private Integer price;

	@Column(name = "maintenance_cost", nullable = true)
	private Integer maintenanceCost;
}
