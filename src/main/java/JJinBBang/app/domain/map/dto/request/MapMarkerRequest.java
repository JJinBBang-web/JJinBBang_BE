package JJinBBang.app.domain.map.dto.request;

import JJinBBang.app.domain.common.dto.item.Filters;
import JJinBBang.app.domain.map.dto.item.Bounds;
import jakarta.validation.constraints.NotNull;

public record MapMarkerRequest(
	@NotNull(message = "{bounds.notNull}")
	Bounds bounds,

	@NotNull(message = "{filter.notNull}")
	Filters filters
) {
}
