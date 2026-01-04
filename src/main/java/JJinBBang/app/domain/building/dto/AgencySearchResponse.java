package JJinBBang.app.domain.building.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record AgencySearchResponse(
	List<AgencySearchItem> items,
	Integer num,
	String nextCursor,   // 다음 요청에 넘길 커서(마지막 등록번호)
	Boolean hasMore      // 더 가져올 가능성(일단 items.size == num 기준)
) {
	public static AgencySearchResponse of(List<AgencySearchItem> items, Integer num, String nextCursor, Boolean hasMore) {
		return AgencySearchResponse.builder()
			.items(items)
			.num(num)
			.nextCursor(nextCursor)
			.hasMore(hasMore)
			.build();
	}
}
