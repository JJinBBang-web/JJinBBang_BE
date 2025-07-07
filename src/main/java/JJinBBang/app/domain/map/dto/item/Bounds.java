package JJinBBang.app.domain.map.dto.item;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record Bounds(
	@NotNull(message = "{latitude.notNull}")
	@Min(value = -90, message = "{latitude.min}")
	@Max(value = 90, message = "{latitude.max}")
	Double neLat, // 북동쪽 위도

	@NotNull(message = "{longitude.notNull}")
	@Min(value = -180, message = "{longitude.min}")
	@Max(value = 180, message = "{longitude.max}")
	Double neLng, // 북동쪽 경도

	@NotNull(message = "{latitude.notNull}")
	@Min(value = -90, message = "{latitude.min}")
	@Max(value = 90, message = "{latitude.max}")
	Double swLat, // 남서쪽 위도

	@NotNull(message = "{longitude.notNull}")
	@Min(value = -180, message = "{longitude.min}")
	@Max(value = 180, message = "{longitude.max}")
	Double swLng // 남서쪽 경도
) {
}

