package JJinBBang.app.domain.building.dto;

import java.util.List;

import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.global.common.dto.AgencyBuildingInfo;
import JJinBBang.app.global.common.dto.DormitoryBuildingInfo;
import JJinBBang.app.global.common.dto.GeneralBuildingInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
public record BuildingDetailResponse(
		@JsonInclude(JsonInclude.Include.NON_NULL)
		GeneralBuildingInfo generalBuildingInfo,

		@JsonInclude(JsonInclude.Include.NON_NULL)
		DormitoryBuildingInfo dormitoryBuildingInfo,

		@JsonInclude(JsonInclude.Include.NON_NULL)
		AgencyBuildingInfo agencyBuildingInfo,

		ReviewImages reviewImages,
		List<KeywordCount> keywords
) {
	public static BuildingDetailResponse ofGeneral(Buildings building, Boolean liked, List<KeywordCount> keywords) {
		return BuildingDetailResponse.builder()
			.generalBuildingInfo(GeneralBuildingInfo.of(building, liked))
			.reviewImages(ReviewImages.from(building))
			.keywords(keywords)
			.build();
	}

	public static BuildingDetailResponse ofDormitory(Buildings building, Boolean liked, List<KeywordCount> keywords) {
		return BuildingDetailResponse.builder()
				.dormitoryBuildingInfo(DormitoryBuildingInfo.of(building, liked))
				.reviewImages(ReviewImages.from(building))
				.keywords(keywords)
				.build();
	}

	public static BuildingDetailResponse ofAgency(Agencies agency, Boolean liked, List<KeywordCount> keywords) {
		return BuildingDetailResponse.builder()
				.agencyBuildingInfo(AgencyBuildingInfo.of(agency, liked))
				.reviewImages(ReviewImages.from(agency))
				.keywords(keywords)
				.build();
	}
}
