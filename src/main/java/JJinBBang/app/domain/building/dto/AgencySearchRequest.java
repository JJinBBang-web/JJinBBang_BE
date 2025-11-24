package JJinBBang.app.domain.building.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AgencySearchRequest(
	@NotBlank(message = "agencyName은 비어 있을 수 없습니다.")
	String agencyName, // 공인중개사 상호명, 필수값
	@Min(1)	@Max(10)
	Integer num, // 한 페이지당 검색 건수 기본값 10
	Integer page // 페이지 번호 1부터 시작
) {

	public AgencySearchRequest {
		if (num == null) {
			num = 10;      // 기본 num
		}
		if (page == null || page < 1) {
			page = 1;      // 기본 page
		}
	}
}
