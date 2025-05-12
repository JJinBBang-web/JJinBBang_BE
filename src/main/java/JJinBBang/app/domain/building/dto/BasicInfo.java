package JJinBBang.app.domain.building.dto;

import JJinBBang.app.domain.building.entity.Buildings;
import lombok.Builder;

@Builder
public record BasicInfo(
	Boolean liked,
	Long id,
	String type,
	String name,
	String address,
	Double rating,
	Integer reviewCount
) {
	public static BasicInfo of(Buildings building, Boolean liked) {
		return BasicInfo.builder()
			.liked(liked)
			.id(building.getId())
			.type(building.getBuildingType())
			.name(building.getBuildingName())
			.address(building.getBuildingAddress())
			.rating(building.getBuildingRating())
			.reviewCount(building.getReviewCount())
			.build();
	}
}
