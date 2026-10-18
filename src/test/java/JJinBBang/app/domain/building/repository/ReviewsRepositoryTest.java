package JJinBBang.app.domain.building.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.domain.building.enums.ReviewType;
import JJinBBang.app.support.JdbcFixture;
import JJinBBang.app.support.MySqlJpaTest;

@MySqlJpaTest
class ReviewsRepositoryTest {

	private static final long USER_ID = 1L;
	private static final LocalDateTime T = LocalDateTime.of(2025, 1, 1, 0, 0);

	@Autowired
	ReviewsRepository reviewsRepository;
	@Autowired
	JdbcTemplate jdbcTemplate;

	JdbcFixture fixture;

	@BeforeEach
	void setUp() {
		fixture = new JdbcFixture(jdbcTemplate);
		fixture.user(USER_ID);
	}

	@Test
	void findLatestByBuildingIds_건물별_가장_최근_리뷰만_반환() {
		fixture.building(1L, "B-1", "건물1", "ROOM", null);
		fixture.building(2L, "B-2", "건물2", "ROOM", null);
		fixture.building(3L, "B-3", "건물3", "ROOM", null);
		fixture.building(4L, "B-4", "건물4", "ROOM", null);
		fixture.generalReview(11L, USER_ID, 1L, null, T);
		fixture.generalReview(12L, USER_ID, 1L, null, T.plusHours(1));
		fixture.generalReview(21L, USER_ID, 2L, null, T.plusHours(2));
		// 조회 대상이 아닌 건물의 리뷰
		fixture.generalReview(41L, USER_ID, 4L, null, T.plusHours(3));

		// 건물 3은 리뷰가 없으므로 결과에 없어야 함
		List<Reviews> result = reviewsRepository.findLatestByBuildingIds(List.of(1L, 2L, 3L));

		assertEquals(Set.of(12L, 21L), ids(result));
	}

	@Test
	void findLatestByBuildingIds_createdAt이_같으면_모두_반환() {
		fixture.building(1L, "B-1", "건물1", "ROOM", null);
		fixture.generalReview(11L, USER_ID, 1L, null, T);
		fixture.generalReview(12L, USER_ID, 1L, null, T);

		// 하나로 고르는 것은 SearchInfo 의 몫
		List<Reviews> result = reviewsRepository.findLatestByBuildingIds(List.of(1L));

		assertEquals(Set.of(11L, 12L), ids(result));
	}

	@Test
	void findLatestByAgencyIdsAndDtype_해당_dtype_리뷰_중에서만_최신을_반환() {
		fixture.agency(1L, "AG-1", "중개사1");
		fixture.agency(2L, "AG-2", "중개사2");
		fixture.agencyReview(11L, USER_ID, 1L, T);
		// 중개사 1에 더 최근 리뷰가 있지만 AGENCY 타입이 아님
		fixture.generalReview(12L, USER_ID, null, 1L, T.plusHours(1));
		fixture.agencyReview(21L, USER_ID, 2L, T);
		fixture.agencyReview(22L, USER_ID, 2L, T.plusHours(2));

		List<Reviews> result = reviewsRepository.findLatestByAgencyIdsAndDtype(List.of(1L, 2L), ReviewType.AGENCY);

		assertEquals(Set.of(11L, 22L), ids(result));
	}

	private static Set<Long> ids(List<Reviews> reviews) {
		return reviews.stream().map(Reviews::getId).collect(Collectors.toSet());
	}
}
