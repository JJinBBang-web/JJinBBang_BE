package JJinBBang.app.domain.building.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VWorldEBOfficeResponse(
	@JsonProperty("EDOffices")
	EDOffices edOffices,

	@JsonProperty("response")
	ResponseMeta response
) {

	/**
	 * 레코드가 1개 이상일 때 내려오는 EDOffices 블록
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record EDOffices(
		@JsonProperty("field")
		List<EDOfficeField> field,

		@JsonProperty("pageNo")
		Integer pageNo,

		@JsonProperty("resultCode")
		String resultCode,

		@JsonProperty("totalCount")
		Integer totalCount,

		@JsonProperty("numOfRows")
		Integer numOfRows,

		@JsonProperty("resultMsg")
		String resultMsg
	) {
	}

	/**
	 * 레코드가 0개일 때 내려오는 response 블록
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ResponseMeta(
		@JsonProperty("pageNo")
		Integer pageNo,

		@JsonProperty("resultCode")
		String resultCode,

		@JsonProperty("totalCount")
		Integer totalCount,

		@JsonProperty("numOfRows")
		Integer numOfRows,

		@JsonProperty("resultMsg")
		String resultMsg
	) {
	}

	/**
	 * 실제 중개사무소 한 건 (EDOffices.field 의 원소)
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record EDOfficeField(
		@JsonProperty("jurirno") // 등록번호
		String jurirno,

		@JsonProperty("brkrNm") // 중개업자명
		String brkrNm,

		@JsonProperty("ldCode") // 시군구코드
		String ldCode,

		@JsonProperty("ldCodeNm") // 시군구명
		String ldCodeNm,

		@JsonProperty("registDe") // 등록일자
		String registDe,

		@JsonProperty("sttusSeCodeNm") // 상태구분명
		String sttusSeCodeNm,

		@JsonProperty("rdnmadrcode") // 도로명주소코드
		String rdnmadrcode,

		@JsonProperty("mnnmadr") // 지번주소
		String mnnmadr,

		@JsonProperty("rdnmadr") // 도로명주소
		String rdnmadr,

		@JsonProperty("estbsBeginDe") // 보증설정시작일
		String estbsBeginDe,

		@JsonProperty("lastUpdtDt") // 데이터기준일자
		String lastUpdtDt,

		@JsonProperty("estbsEndDe") // 보증설정종료일
		String estbsEndDe,

		@JsonProperty("sttusSeCode") // 상태구분코드
		String sttusSeCode,

		@JsonProperty("bsnmCmpnm") // 사업자상호
		String bsnmCmpnm
	) {
	}
}
