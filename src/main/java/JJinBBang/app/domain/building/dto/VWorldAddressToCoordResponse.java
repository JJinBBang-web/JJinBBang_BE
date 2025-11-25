package JJinBBang.app.domain.building.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VWorldAddressToCoordResponse(

	@JsonProperty("response")
	Body response   // ← 안쪽 진짜 payload

) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Body(
		@JsonProperty("service")
		Service service,

		@JsonProperty("status")
		String status,          // OK / NOT_FOUND / ERROR

		@JsonProperty("input")
		Input input,            // simple=true면 생략될 수 있음

		@JsonProperty("refined")
		Refined refined,        // refine=false 또는 simple=true면 생략될 수 있음

		@JsonProperty("result")
		Result result,          // 정상일 때 좌표 정보

		@JsonProperty("error")
		Error error             // status=ERROR일 때만 존재
	) {

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Service(
		@JsonProperty("name")
		String name,

		@JsonProperty("version")
		String version,

		@JsonProperty("operation")
		String operation,

		@JsonProperty("time")
		String time
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Input(
		@JsonProperty("type")
		String type,      // ROAD / PARCEL
		@JsonProperty("address")
		String address
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Refined(
		@JsonProperty("text")
		String text,      // 정제된 전체 주소 문자열
		@JsonProperty("structure")
		Structure structure
	) {
		@JsonIgnoreProperties(ignoreUnknown = true)
		public record Structure(
			@JsonProperty("level0")
			String level0,   // 국가

			@JsonProperty("level1")
			String level1,   // 시·도

			@JsonProperty("level2")
			String level2,   // 시·군·구

			@JsonProperty("level3")
			String level3,   // (일반구)구

			@JsonProperty("level4L")
			String level4L,  // (도로) 도로명 / (지번) 법정동 명

			@JsonProperty("level4A")
			String level4A,  // (도로) 행정동 명

			@JsonProperty("level4AC")
			String level4AC, // (도로) 행정동 코드

			@JsonProperty("level5")
			String level5,   // (도로) 길 / (지번) 번지

			@JsonProperty("detail")
			String detail    // 상세주소
		) {
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Result(
		@JsonProperty("crs")
		String crs,      // EPSG:4326 등

		@JsonProperty("point")
		Point point
	) {
		@JsonIgnoreProperties(ignoreUnknown = true)
		public record Point(
			@JsonProperty("x")
			double x,      // 경도
			@JsonProperty("y")
			double y       // 위도
		) {
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Error(
		@JsonProperty("level")
		int level,

		@JsonProperty("code")
		String code,

		@JsonProperty("text")
		String text
	) {
	}
}
