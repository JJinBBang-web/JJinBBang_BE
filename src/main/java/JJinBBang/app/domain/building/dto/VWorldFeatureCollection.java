package JJinBBang.app.domain.building.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VWorldFeatureCollection(
	String type,
	@JsonProperty("features") List<VWorldFeature> features,
	@JsonProperty("numberMatched") Object numberMatched,   // 문자열로 올 수도 있어서 Object로 방어
	@JsonProperty("totalFeatures") Integer totalFeatures
) {
	public int totalCountFallback(int fallback) {
		if (totalFeatures != null)
			return totalFeatures;
		if (numberMatched == null)
			return fallback;
		try {
			return Integer.parseInt(String.valueOf(numberMatched));
		} catch (Exception e) {
			return fallback;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record VWorldFeature(
		String id,
		VWorldGeometry geometry,
		VWorldProperties properties,
		List<Double> bbox
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record VWorldGeometry(
		String type,
		List<Double> coordinates
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record VWorldProperties(
		@JsonProperty("bsnm_cmpnm") String agencyName,
		@JsonProperty("brkpg_regist_no") String registNo,
		String rdnmadr,
		String mnnmadr,
		@JsonProperty("x_crdnt") Double xCrdnt,
		@JsonProperty("y_crdnt") Double yCrdnt
	) {
	}
}
