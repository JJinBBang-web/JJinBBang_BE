package JJinBBang.app.domain.building.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

	// 기숙사 리뷰 -> 기숙사 시설
	@OneToMany(mappedBy = "dormitoryReview", cascade = CascadeType.ALL)
	private List<DormitoryFacilities> dormitoryFacilities = new ArrayList<>();

	@Builder
	private DormReviews(
		Long id,
		Integer likesCount,
		ReviewType dtype,
		String thumbnailImage,
		String content,
		List<KeywordType> tags,
		BigDecimal rating,
		Users user,
		Buildings building,
		Agencies agency,
		List<ReviewLikes> reviewLikes,

		Floor floor,
		Integer capacity,
		Integer dormFee,
		String currentRegion,
		Double currentGrade,
		List<DormitoryFacilities> dormitoryFacilities
	) {
		super(id, dtype, likesCount, thumbnailImage, content, tags, rating, user, building, agency, reviewLikes);
		this.floor = floor;
		this.capacity = capacity;
		this.dormFee = dormFee;
		this.currentRegion = currentRegion;
		this.currentGrade = currentGrade;
		this.dormitoryFacilities = dormitoryFacilities;
	}
}
