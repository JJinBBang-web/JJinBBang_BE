package JJinBBang.app.domain.building.dto;

import java.util.Objects;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import lombok.Builder;

@Builder
public record VWorld2DDataRequest(
	String request,     // 요청 서비스 오퍼레이션 (GetFeature, GetFeatureType)
	String key,			// 발급받은 api key
	Integer size,      	// 한 페이지에 출력될 응답결과 건수	(숫자, default(10), min(1), max(1000))
	Integer page,		// 응답결과 페이지 번호 (숫자, 1(기본값))
	String data,		// 조회할 데이터 (LT_C_SPBD)
	String geomFilter,  // 지오메트리 필터
	Integer buffer		// geomFilter파라미터에 입력한 feature를 buffer(거리, 단위:m)만큼 확장 (숫자, 기본값:0)
) {

	/** 기본값을 채운 빌더성 팩토리 */
	public static VWorld2DDataRequest of(
		String apiKey,
		String geomFilter,
		Integer buffer
	) {
		return new VWorld2DDataRequest(
			"GetFeature",
			Objects.requireNonNull(apiKey),
			1000,
			1,
			"LT_C_SPBD",
			Objects.requireNonNull(geomFilter),
			buffer != null ? buffer : 0
		);
	}

	/** 포인트 + 버퍼(m)로 조회 (경위도 순서: lon, lat) */
	public static VWorld2DDataRequest byPoint(String apiKey, double lon, double lat, int bufferMeters) {
		String gf = "POINT(" + lon + " " + lat + ")";
		return of(apiKey, gf, bufferMeters);
	}

	public MultiValueMap<String, String> toQueryParams() {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("request", request);
		map.add("key", key);
		map.add("size", size.toString());
		map.add("page", page.toString());
		map.add("format", "json");
		map.add("data", data);
		map.add("geomFilter", geomFilter);
		map.add("buffer", buffer.toString());
		return map;
	}
}
