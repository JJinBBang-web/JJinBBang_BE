package JJinBBang.app.domain.building.dto;

import lombok.Builder;

@Builder
public record DormitoryItem(
	String campusName,
	Long dormitoryId,
	String dormitoryName,
	String dormitoryAddress
) {
	public static DormitoryItem of(String campusName, Long dormitoryId, String dormitoryName, String dormitoryAddress) {
		return DormitoryItem.builder()
			.campusName(campusName)
			.dormitoryId(dormitoryId)
			.dormitoryName(dormitoryName)
			.dormitoryAddress(dormitoryAddress)
			.build();
	}
}
