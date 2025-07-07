package JJinBBang.app.domain.map.dto.response;

import java.util.List;

import JJinBBang.app.global.common.dto.InfoDto;
import lombok.Builder;

@Builder
public record MapItemDetailResponse(
	Integer num, // 불러올 아이템 수
	Integer page, // 페이지 번호
	Integer itemNum, // 총 아이템 수
	List<InfoDto> items // 아이템, 후기별 or 건물별

) {
	public static MapItemDetailResponse of(Integer num, Integer page, Integer itemNum,
		List<InfoDto> items) {
		return MapItemDetailResponse.builder()
			.num(num)
			.page(page)
			.itemNum(itemNum)
			.items(items)
			.build();
	}
}
