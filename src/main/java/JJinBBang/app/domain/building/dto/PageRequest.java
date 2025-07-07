package JJinBBang.app.domain.building.dto;

import org.springframework.data.domain.AbstractPageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import JJinBBang.app.domain.common.enums.SortType;
import lombok.Getter;

@Getter
public class PageRequest extends AbstractPageRequest {
	private final Integer num;
	private final Integer page;
	private final SortType sortBy;

	public PageRequest(Integer page,
		Integer num,
		SortType sortBy) {
		super(
			page != null ? page : 0,
			num   != null ? num   : 10
		);
		this.page   = page   != null ? page   : 0;
		this.num    = num    != null ? num    : 10;
		this.sortBy = sortBy != null ? sortBy : SortType.LATEST;
	}

	@Override
	public Sort getSort() {
		return switch (sortBy) {
			case LATEST -> Sort.by("createdAt").descending();
			case LIKES -> Sort.by("likesCount").descending();
			case STARS -> Sort.by("rating").descending();
			default -> Sort.by("createdAt").descending();
		};
	}

	@Override
	public Pageable withPage(int pageNumber) {
		return new PageRequest(pageNumber, this.num, this.sortBy);
	}

	@Override
	public Pageable next() {
		return withPage(getPage() + 1);
	}

	@Override
	public Pageable previous() {
		return withPage(getPage() - 1);
	}

	@Override
	public Pageable first() {
		return withPage(0);
	}
}
