package JJinBBang.app.domain.building.dto;

import org.springframework.util.MultiValueMap;

import lombok.Builder;

/**
 * vworld Geocoder API (주소 → 좌표, getCoord)의 요청 파라미터 DTO.
 * 이걸 가지고 쿼리스트링을 만들어서 호출하면 됨.
 */
@Builder
public record VWorldAddressToCoordRequest(
	String service,      // 기본값: "address"
	String version,      // 기본값: "2.0"
	String request,      // 기본값: "GetCoord" 또는 "getCoord"
	String format,       // 기본값: "json"
	String errorFormat,  // 생략 가능 (null이면 format과 동일)
	String type,         // "ROAD" (도로명) 또는 "PARCEL" (지번) (필수)
	String address,      // 검색할 주소 문자열 (필수)
	Boolean refine,      // 기본값: true
	Boolean simple,      // 기본값: false
	String crs,          // 기본값: "epsg:4326"
	String callback      // JSONP 쓸 때만, 보통 null
) {

	/**
	 * 도로명주소용 편의 팩토리.
	 * - service=address
	 * - version=2.0
	 * - request=getCoord
	 * - format=json
	 * - refine=true
	 * - simple=false
	 * - crs=epsg:4326
	 */
	public static VWorldAddressToCoordRequest road(String address) {
		return VWorldAddressToCoordRequest.builder()
			.service("address")
			.version("2.0")
			.request("getCoord")
			.format("json")
			.type("ROAD")
			.address(address)
			.refine(true)
			.simple(false)
			.crs("epsg:4326")
			.build();
	}

	/**
	 * 지번주소용 편의 팩토리 (type=PARCEL).
	 */
	public static VWorldAddressToCoordRequest parcel(String address) {
		return VWorldAddressToCoordRequest.builder()
			.service("address")
			.version("2.0")
			.request("getCoord")
			.format("json")
			.type("PARCEL")
			.address(address)
			.refine(true)
			.simple(false)
			.crs("epsg:4326")
			.build();
	}

	public MultiValueMap<String, String> toQueryParams(String apiKey) {
		MultiValueMap<String, String> map = new org.springframework.util.LinkedMultiValueMap<>();
		map.add("service", service);
		map.add("version", version);
		map.add("request", request);
		map.add("key", apiKey);
		map.add("format", format);
		if (errorFormat != null) {
			map.add("errorFormat", errorFormat);
		}
		map.add("type", type);
		map.add("address", address);
		map.add("refine", refine.toString());
		map.add("simple", simple.toString());
		map.add("crs", crs);
		if (callback != null) {
			map.add("callback", callback);
		}
		return map;
	}
}
