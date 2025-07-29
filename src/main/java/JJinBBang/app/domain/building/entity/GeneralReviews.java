package JJinBBang.app.domain.building.entity;

import JJinBBang.app.domain.building.enums.BuildingType;
import java.math.BigDecimal;
import java.util.List;

import JJinBBang.app.domain.building.enums.ContractType;
import JJinBBang.app.domain.building.enums.Floor;
import JJinBBang.app.domain.building.enums.ReviewType;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.common.enums.KeywordType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
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

	@Builder
	public GeneralReviews(
		Long id,
		ReviewType dtype,
		Integer likesCount,
		String thumbnailImage,
		String content,
		List<KeywordType> tags,
		BigDecimal rating,
		BuildingType buildingType,
		Users user,
		Buildings building,
		Agencies agency,
		List<ReviewLikes> reviewLikes,

		Floor floor,
		Double area,
		ContractType contractType,
		Integer deposit,
		Integer price,
		Integer maintenanceCost
	) {
		super(id, dtype, likesCount, thumbnailImage, content, tags, rating, buildingType, user, building, agency, reviewLikes);
		this.floor           = floor;
		this.area            = area;
		this.contractType    = contractType;
		this.deposit         = deposit;
		this.price           = price;
		this.maintenanceCost = maintenanceCost;
	}
}
