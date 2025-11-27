package JJinBBang.app.domain.building.dto;

import lombok.Builder;

@Builder
public record AgencySearchItem(
	String registerNumber, // 개설등록번호
	String companyName, // 상호명
	String brokerName, // 중개사명
	String roadAddress, // 도로명주소
	String jibunAddress, // 지번주소
	Double latitude, // 위도
	Double longitude // 경도
) {

	public static AgencySearchItem of (
		String registerNumber,
		String companyName,
		String brokerName,
		String roadAddress,
		String jibunAddress,
		Double latitude,
		Double longitude
	) {
		return AgencySearchItem.builder()
			.registerNumber(registerNumber)
			.companyName(companyName)
			.brokerName(brokerName)
			.roadAddress(roadAddress)
			.jibunAddress(jibunAddress)
			.latitude(latitude)
			.longitude(longitude)
			.build();
	}
}
