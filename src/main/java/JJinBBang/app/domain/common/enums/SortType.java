package JJinBBang.app.domain.common.enums;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;

@AllArgsConstructor
public enum SortType {

	// enum 상수에 [속성명, 방향]을 정의
	LATEST("createdAt", Sort.Direction.DESC),
	LIKES ("likesCount", Sort.Direction.DESC),
	STARS ("rating",    Sort.Direction.DESC),
	// 추천순는 임시로 likesCount를 기준으로 정렬
	RCMND ("likesCount", Sort.Direction.DESC);

	private final String property;
	private final Sort.Direction direction;

	public Sort toSort() {
		return Sort.by(direction, property);
	}
}