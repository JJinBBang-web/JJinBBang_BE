package JJinBBang.app.domain.building.repository.custom;

import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.QAgencies;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
@RequiredArgsConstructor
public class AgenciesRepositoryImpl implements AgenciesRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Agencies> searchAgencies(String keyword) {
		QAgencies a = QAgencies.agencies;

		BooleanBuilder builder = new BooleanBuilder();
		if (keyword != null && !keyword.isEmpty()) {
			builder.and(a.name.containsIgnoreCase(keyword)
				.or(a.address.containsIgnoreCase(keyword)));
		}

		return queryFactory.selectFrom(a)
			.where(builder)
			.fetch();
	}
}