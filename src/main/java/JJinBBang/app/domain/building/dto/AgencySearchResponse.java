package JJinBBang.app.domain.building.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record AgencySearchResponse(
	List<AgencySearchItem> items,
	Integer num,
	Integer page,
	Integer totalCount
) {

	public static AgencySearchResponse of(List<AgencySearchItem> items, Integer num, Integer page, Integer totalCount) {
		return AgencySearchResponse.builder()
			.items(items)
			.num(num)
			.page(page)
			.totalCount(totalCount)
			.build();
	}
}
