package JJinBBang.app.domain.map.dto.request;

import java.util.List;

import JJinBBang.app.domain.common.enums.SortType;
import JJinBBang.app.global.common.enums.ViewType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record nearByMapItemRequest(
	@NotNull(message = "{pagination.num.notNull}")
	@Min(value = 1, message = "{pagination.num.min}")
	@Max(value = 30, message = "{pagination.num.max}")
	Integer num,   // 불러올 데이터 개수

	@NotNull(message = "{pagination.page.notNull}")
	@Min(value = 0, message = "{pagination.page.min}")
	Integer page,  // 페이지 번호

	@NotNull(message = "{filter.viewType.notNull}")
	@Pattern(regexp = "^(ALL|BUILDING|REVIEW)$", message = "{filter.viewType.invalid}")
	ViewType type, // 보기 유형

	@NotNull(message = "{sortType.notNull}")
	@Pattern(regexp = "^(LATEST|LIKES|STARS|RCMND)$", message = "{sortType.invalid}")
	SortType sortBy, // 정렬 기준

	@NotNull(message = "{idList.notNull}")
	List<Long> idList
) {
}
