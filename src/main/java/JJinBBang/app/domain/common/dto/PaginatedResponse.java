package JJinBBang.app.domain.common.dto;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import lombok.Builder;

@Builder
public record PaginatedResponse<T>(
	int num,		// 불러온 개수
	int page,		// 현재 페이지
	Long itemNum,	// 전체 검색 결과 수
	List<T> items	// 제네릭 타입 리스트
) {

	public static <T> PaginatedResponse<T> of(Page<T> page) {
		return PaginatedResponse.<T>builder()
			.num(page.getNumberOfElements())
			.page(page.getNumber())
			.itemNum(page.getTotalElements())
			.items(page.getContent())
			.build();
	}

	public static <E, T> PaginatedResponse<T> of(
		Page<E> page,
		Function<? super E, ? extends T> mapper
	) {
		List<T> mapped = page.getContent()
			.stream()
			.map(mapper)
			.collect(Collectors.toList());

		return PaginatedResponse.<T>builder()
			.num(mapped.size())
			.page(page.getNumber())
			.itemNum(page.getTotalElements())
			.items(mapped)
			.build();
	}
}
