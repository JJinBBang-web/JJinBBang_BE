package JJinBBang.app.domain.building.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AgencySearchRequest(
	@NotBlank(message = "agencyName은 비어 있을 수 없습니다.")
	String agencyName,   // 상호명(필수)

	@Min(1) @Max(10)
	Integer num,         // 페이지 크기(기본 10)

	String cursor        // 이전 페이지 마지막 registerNumber(없으면 첫 페이지)
) {
	public AgencySearchRequest {
		if (num == null) num = 10;
		if (cursor != null && cursor.isBlank()) cursor = null;
	}
}
