package JJinBBang.app.domain.map.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import JJinBBang.app.domain.building.enums.BuildingType;
import JJinBBang.app.domain.building.repository.ReviewDetailsRepository;
import JJinBBang.app.domain.building.service.SearchInfo;
import JJinBBang.app.domain.common.dto.PaginatedResponse;
import JJinBBang.app.domain.common.dto.item.Filters;
import JJinBBang.app.domain.common.enums.SortType;
import JJinBBang.app.domain.map.dto.request.SearchMarkerRequest;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.common.dto.SearchInfoDto;
import JJinBBang.app.global.common.enums.ViewType;
import JJinBBang.app.support.JdbcFixture;
import JJinBBang.app.support.MySqlJpaTest;
import jakarta.persistence.EntityManager;

/**
 * 지도 검색(searchMarker)의 N+1 회귀를 막습니다.
 * 검색 후보 수가 10배가 되어도 실행되는 SQL 수는 같아야 합니다.
 */
@MySqlJpaTest
@Import({MapServiceImpl.class, SearchInfo.class})
class MapServiceImplQueryCountTest {

	private static final long USER_ID = 1L;
	private static final long CAMPUS_ID = 1L;
	private static final LocalDateTime T = LocalDateTime.of(2025, 1, 1, 0, 0);

	// SearchInfo 가 의존하는 Mongo 저장소. 지도 검색 경로에서는 호출되지 않는다
	@MockitoBean
	ReviewDetailsRepository reviewDetailsRepository;

	@Autowired
	MapService mapService;
	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	EntityManager entityManager;

	JdbcFixture fixture;

	@BeforeEach
	void setUp() {
		fixture = new JdbcFixture(jdbcTemplate);
		fixture.university(1L);
		fixture.campus(CAMPUS_ID, 1L);
		fixture.user(USER_ID);
		seedGroup("알파", 1_000L, 2);
		seedGroup("베타", 2_000L, 20);
	}

	@ParameterizedTest(name = "{0} 보기, 로그인 {1}")
	@CsvSource({"BUILDING, true", "BUILDING, false", "REVIEW, true", "REVIEW, false"})
	void searchMarker_후보가_늘어도_SQL_수가_같다(ViewType viewType, boolean loggedIn) {
		Users user = loggedIn ? entityManager.find(Users.class, USER_ID) : null;

		Measured small = measure(() -> search("알파", viewType, user));
		Measured large = measure(() -> search("베타", viewType, user));

		// 두 검색어의 후보 수가 실제로 달라야 비교가 의미 있음
		assertTrue(large.itemNum() > small.itemNum());
		// 통계가 꺼져 있으면 둘 다 0 이 되어 비교가 무의미해짐
		assertTrue(small.sqlCount() > 0);
		assertEquals(small.sqlCount(), large.sqlCount());
	}

	/**
	 * 검색어 keyword 로 찾히는 후보를 count 세트 만듭니다.
	 * 한 세트는 일반 건물(리뷰 2개), 기숙사(리뷰 1개), 중개사 건물과 연결된 중개사, 독립 중개사로 구성됩니다.
	 */
	private void seedGroup(String keyword, long idBase, int count) {
		for (int i = 0; i < count; i++) {
			long id = idBase + i * 10L;

			fixture.building(id + 1, "B-" + (id + 1), keyword + "빌 " + i, "ROOM", null);
			fixture.generalReview((id + 1) * 10 + 1, USER_ID, id + 1, null, T.plusMinutes(id));
			fixture.generalReview((id + 1) * 10 + 2, USER_ID, id + 1, null, T.plusMinutes(id + 1));
			fixture.buildingLike(id + 1, USER_ID);
			fixture.reviewLike((id + 1) * 10 + 1, USER_ID);

			fixture.building(id + 2, "B-" + (id + 2), keyword + "기숙사 " + i, "DORMITORY", CAMPUS_ID);
			fixture.dormReview((id + 2) * 10 + 1, USER_ID, id + 2, T.plusMinutes(id + 2));

			// 중개사 건물은 buildingCode == agencySerial 로 중개사와 연결된다
			fixture.building(id + 3, "AG-" + (id + 3), keyword + "부동산 건물 " + i, "AGENCY", null);
			fixture.agency(id + 3, "AG-" + (id + 3), keyword + "부동산 " + i);
			fixture.agencyReview((id + 3) * 10 + 1, USER_ID, id + 3, T.plusMinutes(id + 3));

			fixture.agency(id + 4, "AG-" + (id + 4), keyword + "공인중개사 " + i);
			fixture.agencyReview((id + 4) * 10 + 1, USER_ID, id + 4, T.plusMinutes(id + 4));
			fixture.agencyLike(id + 4, USER_ID);
		}
	}

	private PaginatedResponse<SearchInfoDto> search(String keyword, ViewType viewType, Users user) {
		Filters filters = new Filters(viewType, List.of(BuildingType.ALL), "ALL", null,
				null, null, null, null, null, null);
		return mapService.searchMarker(new SearchMarkerRequest(keyword, 10, 1, SortType.LATEST, filters), user);
	}

	/** 영속성 컨텍스트를 비운 뒤 한 번 검색하고, 그동안 실행된 SQL 수를 셉니다 */
	private Measured measure(Supplier<PaginatedResponse<SearchInfoDto>> search) {
		entityManager.flush();
		entityManager.clear();
		Statistics statistics = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
		statistics.clear();

		PaginatedResponse<SearchInfoDto> response = search.get();

		return new Measured(response.itemNum(), statistics.getPrepareStatementCount());
	}

	private record Measured(long itemNum, long sqlCount) {
	}
}
