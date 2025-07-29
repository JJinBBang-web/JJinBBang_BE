package JJinBBang.app.domain.building.entity;

import JJinBBang.app.domain.building.enums.BuildingType;
import java.math.BigDecimal;
import java.util.List;

import JJinBBang.app.domain.building.enums.ReviewType;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.common.enums.KeywordType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "agency_reviews")
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "review_id")
@DiscriminatorValue("AGENCY")
public class AgencyReviews extends Reviews {
	@Builder
	private AgencyReviews(
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
		List<ReviewLikes> reviewLikes
	) {
		super(id, dtype, likesCount, thumbnailImage, content, tags, rating, buildingType, user, building, agency, reviewLikes);
	}
}
