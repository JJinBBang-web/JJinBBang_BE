package JJinBBang.app.domain.building.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.AgencyReviews;
import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.entity.GeneralReviews;
import JJinBBang.app.domain.building.enums.BuildingType;
import JJinBBang.app.domain.building.enums.ContractType;
import JJinBBang.app.domain.building.enums.Floor;
import JJinBBang.app.domain.building.enums.ReviewType;
import JJinBBang.app.domain.building.exception.BookmarkNotFoundException;
import JJinBBang.app.domain.building.repository.AgenciesRepository;
import JJinBBang.app.domain.building.repository.BuildingsRepository;
import JJinBBang.app.domain.building.repository.DormReviewsRepository;
import JJinBBang.app.domain.building.repository.GeneralReviewsRepository;
import JJinBBang.app.domain.building.repository.ReviewDetailsRepository;
import JJinBBang.app.domain.building.repository.ReviewsRepository;
import JJinBBang.app.global.common.dto.SearchInfoDto;
import JJinBBang.app.global.common.enums.ViewType;

/**
 * 지도 검색 목록 조립 로직(*SearchWithBound)을 검증합니다.
 * 조회되지 않은 연관 데이터는 Mockito 기본값(빈 목록)으로 처리됩니다.
 */
@ExtendWith(MockitoExtension.class)
class SearchInfoTest {

	@Mock
	BuildingsRepository buildingsRepository;
	@Mock
	ReviewsRepository reviewsRepository;
	@Mock
	AgenciesRepository agenciesRepository;
	@Mock
	GeneralReviewsRepository generalReviewsRepository;
	@Mock
	DormReviewsRepository dormReviewsRepository;
	@Mock
	ReviewDetailsRepository reviewDetailsRepository;

	@InjectMocks
	SearchInfo searchInfo;

	@Test
	void buildingSearchWithBound_좋아요한_건물만_liked() {
		Buildings liked = building(1L, "ROOM");
		Buildings notLiked = building(2L, "ROOM");

		List<SearchInfoDto> result = searchInfo.buildingSearchWithBound(List.of(liked, notLiked), Set.of(1L));

		assertTrue(result.get(0).generalBuildingInfo().liked());
		assertFalse(result.get(1).generalBuildingInfo().liked());
	}

	@Test
	void buildingSearchWithBound_최신_리뷰가_동률이면_ID가_큰_리뷰_사용() {
		Buildings building = building(1L, "ROOM");
		// createdAt 이 같은 리뷰 두 개가 조회된 상황
		when(reviewsRepository.findLatestByBuildingIds(anyCollection()))
				.thenReturn(List.of(generalReview(12L, building), generalReview(11L, building)));

		List<SearchInfoDto> result = searchInfo.buildingSearchWithBound(List.of(building), Set.of());

		assertEquals("thumb-12.jpg", result.get(0).image());
	}

	@Test
	void buildingSearchWithBound_중개사_건물은_연결된_중개사_정보로_표시() {
		Buildings building = building(1L, "AGENCY");
		Agencies agency = agency(7L, building.getBuildingCode());
		when(agenciesRepository.findAllByAgencySerialIn(anyCollection())).thenReturn(List.of(agency));
		when(reviewsRepository.findLatestByAgencyIdsAndDtype(anyCollection(), eq(ReviewType.AGENCY)))
				.thenReturn(List.of(agencyReview(70L, agency)));

		List<SearchInfoDto> result = searchInfo.buildingSearchWithBound(List.of(building), Set.of(1L));

		SearchInfoDto item = result.get(0);
		assertEquals(7L, item.agencyBuildingInfo().id());
		assertTrue(item.agencyBuildingInfo().liked());
		assertEquals("후기 70", item.reviewInfo().content());
	}

	@Test
	void buildingSearchWithBound_연결된_중개사가_없는_중개사_건물_예외() {
		Buildings building = building(1L, "AGENCY");

		assertThrows(BookmarkNotFoundException.class,
				() -> searchInfo.buildingSearchWithBound(List.of(building), Set.of()));
	}

	@Test
	void agencySearchWithBound_리뷰_보기에서_최신_리뷰_표시() {
		Agencies agency = agency(7L, "AG-7");
		when(reviewsRepository.findLatestByAgencyIdsAndDtype(anyCollection(), eq(ReviewType.AGENCY)))
				.thenReturn(List.of(agencyReview(70L, agency)));

		List<SearchInfoDto> result = searchInfo.agencySearchWithBound(List.of(agency), Set.of(7L), ViewType.REVIEW);

		assertEquals(70L, result.get(0).agencyReviewInfo().id());
		assertTrue(result.get(0).agencyReviewInfo().liked());
	}

	@Test
	void agencySearchWithBound_리뷰_보기에서_리뷰가_없으면_중개사_정보로_표시() {
		Agencies agency = agency(7L, "AG-7");

		List<SearchInfoDto> result = searchInfo.agencySearchWithBound(List.of(agency), Set.of(), ViewType.REVIEW);

		assertNull(result.get(0).agencyReviewInfo());
		assertNotNull(result.get(0).agencyBuildingInfo());
	}

	@Test
	void reviewSearchWithBound_좋아요한_리뷰만_liked() {
		Buildings building = building(1L, "ROOM");

		List<SearchInfoDto> result = searchInfo.reviewSearchWithBound(
				List.of(generalReview(11L, building), generalReview(12L, building)), Set.of(12L));

		assertFalse(result.get(0).generalReviewInfo().liked());
		assertTrue(result.get(1).generalReviewInfo().liked());
	}

	@Test
	void reviewSearchWithBound_계약_유형이_없는_일반_후기_예외() {
		GeneralReviews review = GeneralReviews.builder()
				.id(11L)
				.dtype(ReviewType.GENERAL)
				.content("후기 11")
				.buildingType(BuildingType.ROOM)
				.building(building(1L, "ROOM"))
				.floor(Floor.LOW)
				.area(20.0)
				.build();

		assertThrows(BookmarkNotFoundException.class,
				() -> searchInfo.reviewSearchWithBound(List.of(review), Set.of()));
	}

	private static Buildings building(long id, String buildingType) {
		return Buildings.builder()
				.id(id)
				.buildingCode("B-" + id)
				.buildingName("건물" + id)
				.buildingType(buildingType)
				.buildingAddress("건물 주소")
				.buildingLat(35.15)
				.buildingLot(128.10)
				.reviewCount(0)
				.likeCount(0)
				.imagesCount(0)
				.build();
	}

	private static Agencies agency(long id, String agencySerial) {
		return Agencies.builder()
				.agencyId(id)
				.agencySerial(agencySerial)
				.name("중개사" + id)
				.address("중개사 주소")
				.agencyLat(35.15)
				.agencyLot(128.10)
				.likeCount(0)
				.reviewCount(0)
				.imagesCount(0)
				.build();
	}

	private static GeneralReviews generalReview(long id, Buildings building) {
		return GeneralReviews.builder()
				.id(id)
				.dtype(ReviewType.GENERAL)
				.likesCount(0)
				.thumbnailImage("thumb-" + id + ".jpg")
				.content("후기 " + id)
				.buildingType(BuildingType.ROOM)
				.building(building)
				.floor(Floor.LOW)
				.area(20.0)
				.contractType(ContractType.MONTHLY_RENT)
				.build();
	}

	private static AgencyReviews agencyReview(long id, Agencies agency) {
		return AgencyReviews.builder()
				.id(id)
				.dtype(ReviewType.AGENCY)
				.likesCount(0)
				.thumbnailImage("thumb-" + id + ".jpg")
				.content("후기 " + id)
				.buildingType(BuildingType.AGENCY)
				.agency(agency)
				.build();
	}
}
