package JJinBBang.app.domain.building.dto;

import java.util.List;

import JJinBBang.app.domain.building.entity.Buildings;
import lombok.Builder;

@Builder
public record BuildingDetailResponse(
	BasicInfo basicInfo,
	BuildingImages buildingImages,
	List<Keyword> keywords
) {
	public static BuildingDetailResponse of(Buildings building, Boolean liked, List<Keyword> keywords) {
		return BuildingDetailResponse.builder()
			.basicInfo(BasicInfo.from(building, liked))
			.buildingImages(BuildingImages.from(building))
			.keywords(keywords)
			.build();
	}
}
