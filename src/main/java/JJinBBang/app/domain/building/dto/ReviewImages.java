package JJinBBang.app.domain.building.dto;

import java.util.List;

import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.entity.Reviews;
import lombok.Builder;

@Builder
public record ReviewImages(
	Integer count,
	List<String> imageUrl
) {
	public static ReviewImages from(Buildings building) {
		return ReviewImages.builder()
			.count(building.getImagesCount())
			.imageUrl(building.getReviews().stream().map(Reviews::getThumbnailImage).toList())
			.build();
	}

	public static ReviewImages from(Agencies agency) {
		return ReviewImages.builder()
				.count(agency.getImagesCount())
				.imageUrl(agency.getReviews().stream().map(Reviews::getThumbnailImage).toList())
				.build();
	}
}
