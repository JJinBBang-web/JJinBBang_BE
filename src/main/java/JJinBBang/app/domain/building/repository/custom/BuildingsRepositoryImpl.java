package JJinBBang.app.domain.building.repository.custom;

import java.util.List;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.entity.GeneralReviews;
import JJinBBang.app.domain.building.entity.QAgencies;
import JJinBBang.app.domain.building.enums.BuildingType;
import JJinBBang.app.domain.building.enums.ContractType;
import JJinBBang.app.domain.building.repository.custom.BuildingsRepositoryCustom;
import JJinBBang.app.global.common.enums.KeywordType;
import lombok.RequiredArgsConstructor;

import JJinBBang.app.domain.building.entity.QBuildings;
import JJinBBang.app.domain.building.entity.QReviews;
import JJinBBang.app.domain.building.entity.QGeneralReviews;
import JJinBBang.app.domain.common.entity.QCampuses;

@RequiredArgsConstructor
public class BuildingsRepositoryImpl implements BuildingsRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Buildings> findMarkersWithinBounds(
		Double neLat, Double neLng,
		Double swLat, Double swLng,
		List<BuildingType> buildTypes,
		ContractType contractType,
		Integer depositMin, Integer depositMax,
		Integer monthlyRentMin, Integer monthlyRentMax,
		Boolean inMaintenanceCost,
		List<KeywordType> reviewKeywords
	) {
		QBuildings b = QBuildings.buildings;
		QCampuses c = QCampuses.campuses;
		QReviews r = QReviews.reviews;
		QGeneralReviews gr = QGeneralReviews.generalReviews;
		QAgencies a = QAgencies.agencies;

		BooleanBuilder builder = new BooleanBuilder();

		// 1. 위치 필터
		builder.and(b.buildingLat.between(swLat, neLat));
		builder.and(b.buildingLot.between(swLng, neLng));

		// 2. 건물 유형 필터
		if (buildTypes != null && !buildTypes.contains(BuildingType.ALL)) {
			BooleanBuilder typeBuilder = new BooleanBuilder();
			for (BuildingType type : buildTypes) {
				typeBuilder.or(b.buildingType.containsIgnoreCase(type.name()));
			}
			builder.and(typeBuilder);
		}

		// 3. Treat()를 이용한 GeneralReviews 전용 조인
		PathBuilder<GeneralReviews> grPath = new PathBuilder<>(GeneralReviews.class, "generalReview");
		QGeneralReviews treated = new QGeneralReviews(grPath);

		JPAQuery<Buildings> query = queryFactory.selectDistinct(b)
			.from(b)
			.leftJoin(b.reviews, r).fetchJoin()
			.leftJoin(treated).on(r.id.eq(treated.id))
			.leftJoin(b.campus, c).fetchJoin()
			.where(builder);

		// 4. 계약 조건 필터
		if (contractType != null) {
			query.where(treated.contractType.eq(contractType));
		}
		if (depositMin != null) {
			query.where(treated.deposit.goe(depositMin));
		}
		if (depositMax != null) {
			query.where(treated.deposit.loe(depositMax));
		}
		if (monthlyRentMin != null) {
			query.where(treated.price.goe(monthlyRentMin));
		}
		if (monthlyRentMax != null) {
			query.where(treated.price.loe(monthlyRentMax));
		}
		if (inMaintenanceCost != null && inMaintenanceCost) {
			query.where(treated.maintenanceCost.isNotNull());
		}

		return query.fetch();
	}

	@Override
	public List<Buildings> searchBuildings(
		String keyword,
		List<BuildingType> buildTypes,
		ContractType contractType,
		Integer depositMin, Integer depositMax,
		Integer monthlyRentMin, Integer monthlyRentMax,
		Boolean inMaintenanceCost,
		List<KeywordType> reviewKeywords
	) {
		QBuildings b = QBuildings.buildings;
		QCampuses c = QCampuses.campuses;
		QReviews r = QReviews.reviews;
		QGeneralReviews gr = QGeneralReviews.generalReviews;
		QAgencies a = QAgencies.agencies;

		BooleanBuilder builder = new BooleanBuilder();

		if (keyword != null && !keyword.isEmpty()) {
			builder.and(b.buildingName.containsIgnoreCase(keyword)
				.or(b.buildingAddress.containsIgnoreCase(keyword))
				.or(a.name.containsIgnoreCase(keyword))
				.or(a.address.containsIgnoreCase(keyword)));
		}

		if (buildTypes != null && !buildTypes.contains(BuildingType.ALL)) {
			BooleanBuilder typeBuilder = new BooleanBuilder();
			for (BuildingType type : buildTypes) {
				typeBuilder.or(b.buildingType.containsIgnoreCase(type.name()));
			}
			builder.and(typeBuilder);
		}

		PathBuilder<GeneralReviews> grPath = new PathBuilder<>(GeneralReviews.class, "generalReview");
		QGeneralReviews treated = new QGeneralReviews(grPath);

		JPAQuery<Buildings> query = queryFactory.selectDistinct(b)
			.from(b)
			.leftJoin(b.reviews, r).fetchJoin()
			.leftJoin(treated).on(r.id.eq(treated.id))
			.leftJoin(b.campus, c).fetchJoin()
			.leftJoin(a).on(b.buildingCode.stringValue().eq(a.agencySerial))
			.where(builder);

		if (contractType != null) {
			query.where(treated.contractType.eq(contractType));
		}
		if (depositMin != null) {
			query.where(treated.deposit.goe(depositMin));
		}
		if (depositMax != null) {
			query.where(treated.deposit.loe(depositMax));
		}
		if (monthlyRentMin != null) {
			query.where(treated.price.goe(monthlyRentMin));
		}
		if (monthlyRentMax != null) {
			query.where(treated.price.loe(monthlyRentMax));
		}
		if (inMaintenanceCost != null && inMaintenanceCost) {
			query.where(treated.maintenanceCost.isNotNull());
		}

		return query.fetch();
	}
}
