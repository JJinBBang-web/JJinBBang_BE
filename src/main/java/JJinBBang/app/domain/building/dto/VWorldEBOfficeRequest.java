package JJinBBang.app.domain.building.dto;

import org.springframework.util.MultiValueMap;

import lombok.Builder;

@Builder
public record VWorldEBOfficeRequest(
	String bsnmCmpnm, // 사업자상호
	Integer numOfRows, // 검색건수
	Integer pageNo, // 페이지번호
	String format // 응답문서형식(xml, json)
) {
	public static VWorldEBOfficeRequest of(String agencyName, Integer num, Integer page) {
		return VWorldEBOfficeRequest.builder()
			.bsnmCmpnm(agencyName)
			.numOfRows(num)
			.pageNo(page)
			.format("json")
			.build();
	}

	public MultiValueMap<String, String> toQueryParams(String apiKey, String domain) {
		MultiValueMap<String, String> map = new org.springframework.util.LinkedMultiValueMap<>();
		map.add("bsnmCmpnm", bsnmCmpnm);
		map.add("numOfRows", numOfRows.toString());
		map.add("pageNo", pageNo.toString());
		map.add("format", format);
		map.add("key", apiKey);
		map.add("domain", domain);
		return map;
	}
}
