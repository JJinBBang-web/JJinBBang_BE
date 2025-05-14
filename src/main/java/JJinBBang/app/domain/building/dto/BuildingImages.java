package JJinBBang.app.domain.building.dto;

import java.util.List;

import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.entity.Reviews;
import lombok.Builder;

@Builder
public record BuildingImages(
	Integer count,
	List<String> imageUrl
) {
	public static BuildingImages from(Buildings building) {
		return BuildingImages.builder()
			.count(building.getImagesCount())
			.imageUrl(building.getReviews().stream().map(Reviews::getThumbnailImage).toList())
			.build();
	}
}
