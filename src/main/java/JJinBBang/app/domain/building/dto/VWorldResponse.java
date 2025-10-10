package JJinBBang.app.domain.building.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/** vworld 2D Data API - 도로명주소 건물(spbd) 응답 DTO */
public record VWorldResponse(
	@JsonProperty("response") Response response
) {

	public record Response(
		Service service,
		String status,
		Error error,
		@JsonProperty("record") RecordInfo record,
		Page page,
		Result result
	) {
	}
	public record Error(
		Integer level,
		String code,
		String text
	) {
	}
	public record Service(
		String name,
		String version,
		String operation,
		String time // 예: "25(ms)"
	) {
	}

	public record RecordInfo(
		String total,
		String current
	) {
	}

	public record Page(
		String total,
		String current,
		String size
	) {
	}

	public record Result(
		@JsonProperty("featureCollection") FeatureCollection featureCollection
	) {
	}

	public record FeatureCollection(
		String type,                       // "FeatureCollection"
		List<Double> bbox,                 // [minX, minY, maxX, maxY] (없을 수도 있음)
		List<Feature> features
	) {
	}

	public record Feature(
		String type,                       // "Feature"
		GeoJsonGeometry geometry,
		Properties properties,
		String id                          // 예: "LT_C_SPBD.5100132890"
	) {
	}

	public record GeoJsonGeometry(
		String type,
		Object coordinates
	) {
	}

	/** 문서 기준 핵심 속성 + 현장 응답에서 자주 보이는 필드 포함 */
	public record Properties(
		// 공식 식별자
		@JsonProperty("bd_mgt_sn") String bd_mgt_sn,   // 건물관리번호(25자리)

		// 행정구역/도로명
		String sido,
		String sigungu,
		String gu,                     // 읍/면/동 명칭(문서/데이터에 따라 emd_nm 등으로도 표기되는 경우 존재)
		@JsonProperty("rd_nm") String rd_nm,           // 도로명

		// 건물명
		@JsonProperty("buld_nm") String buld_nm,       // 건물명
		@JsonProperty("buld_nm_dc") String buld_nm_dc, // 동/부속명
		@JsonProperty("bul_eng_nm") String bul_eng_nm, // 영문명(있을 때)

		// 건물번호(문서: 본번/부번이 일반적)
		@JsonProperty("bld_s") String bld_s,           // 건물본번
		@JsonProperty("bld_e") String bld_e,           // 건물부번
		@JsonProperty("buld_no") String buld_no,       // 일부 응답에 존재(본번/부번 합성)

		// 층수 등 부가정보
		@JsonProperty("gro_flo_co") String gro_flo_co, // 지상층수
		@JsonProperty("und_flo_co") String und_flo_co, // 지하층수(있을 때)

		// 코드류(있을 때)
		@JsonProperty("sig_cd") String sig_cd,
		@JsonProperty("rn_cd") String rn_cd,
		@JsonProperty("emd_cd") String emd_cd,

		// 참조 키(있을 때)
		String pnu,            // 토지 PNU
		String xpos, String ypos, // 경위도 문자열일 때가 있어 String 권장
		@JsonProperty("poi_chk") String poi_chk
	) {
	}
}
