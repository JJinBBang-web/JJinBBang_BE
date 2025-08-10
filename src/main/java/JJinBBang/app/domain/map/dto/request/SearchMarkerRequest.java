package JJinBBang.app.domain.map.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import JJinBBang.app.domain.common.dto.item.Filters;
import JJinBBang.app.domain.common.enums.SortType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SearchMarkerRequest(

	@NotNull(message = "{keyword.notNull}")
	String keyword,

	@NotNull(message = "{pagination.num.notNull}")
	@Min(value = 1, message = "{pagination.num.min}")
	@Max(value = 30, message = "{pagination.num.max}")
	Integer num,

	@NotNull(message = "{pagination.page.notNull}")
	@Min(value = 0, message = "{pagination.page.min}")
	Integer page,

	@NotNull(message = "{sortType.notNull}")
	SortType sortBy,

	@NotNull(message = "{filter.notNull}")
	Filters filters
) {
}
