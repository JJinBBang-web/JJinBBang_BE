package JJinBBang.app.domain.building.dto;

import java.util.List;

import JJinBBang.app.domain.building.entity.DormitoryFacilities;
import JJinBBang.app.domain.building.enums.UsageType;
import lombok.Builder;

@Builder
public record FacilitiesDto(
	List<String> publicFacilities,
	List<String> privateFacilities,
	Boolean lounge
) {
	public static FacilitiesDto of(List<DormitoryFacilities> facilities) {
		List<String> publicFacilities = facilities.stream()
			.filter(facility -> facility.getUsageType() == UsageType.PUBLIC)
			.map(facility -> facility.getFacility().getName())
			.toList();

		List<String> privateFacilities = facilities.stream()
			.filter(facility -> facility.getUsageType() == UsageType.PRIVATE)
			.map(facility -> facility.getFacility().getName())
			.toList();

		boolean hasLounge = facilities.stream()
			.map(facility -> facility.getFacility().getName())
			.anyMatch("lounge"::equalsIgnoreCase);

		return FacilitiesDto.builder()
			.publicFacilities(publicFacilities)
			.privateFacilities(privateFacilities)
			.lounge(hasLounge)
			.build();
	}
}
