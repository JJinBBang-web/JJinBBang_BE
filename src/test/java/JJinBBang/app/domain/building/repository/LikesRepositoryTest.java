package JJinBBang.app.domain.building.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import JJinBBang.app.support.JdbcFixture;
import JJinBBang.app.support.MySqlJpaTest;

/**
 * 지도 검색에서 쓰는 좋아요 ID 일괄 조회 쿼리를 검증합니다.
 * 사용자 1과 2가 각각 좋아요를 누른 상태에서, 사용자 1이 넘긴 ID 목록 중 자신이 좋아요한 것만 반환되어야 합니다.
 */
@MySqlJpaTest
class LikesRepositoryTest {

	private static final long USER_ID = 1L;
	private static final long OTHER_USER_ID = 2L;
	private static final LocalDateTime T = LocalDateTime.of(2025, 1, 1, 0, 0);

	@Autowired
	BuildingLikesRepository buildingLikesRepository;
	@Autowired
	AgencyLikesRepository agencyLikesRepository;
	@Autowired
	ReviewLikesRepository reviewLikesRepository;
	@Autowired
	JdbcTemplate jdbcTemplate;

	JdbcFixture fixture;

	@BeforeEach
	void setUp() {
		fixture = new JdbcFixture(jdbcTemplate);
		fixture.user(USER_ID);
		fixture.user(OTHER_USER_ID);
	}

	@Test
	void findLikedBuildingIds_본인이_좋아요한_대상_건물만_반환() {
		fixture.building(1L, "B-1", "건물1", "ROOM", null);
		fixture.building(2L, "B-2", "건물2", "ROOM", null);
		fixture.building(3L, "B-3", "건물3", "ROOM", null);
		fixture.buildingLike(1L, USER_ID);
		// 대상 목록에 없는 건물
		fixture.buildingLike(2L, USER_ID);
		// 다른 사용자의 좋아요
		fixture.buildingLike(3L, OTHER_USER_ID);

		Set<Long> result = buildingLikesRepository.findLikedBuildingIds(USER_ID, List.of(1L, 3L));

		assertEquals(Set.of(1L), result);
	}

	@Test
	void findLikedAgencyIds_본인이_좋아요한_대상_중개사만_반환() {
		fixture.agency(1L, "AG-1", "중개사1");
		fixture.agency(2L, "AG-2", "중개사2");
		fixture.agency(3L, "AG-3", "중개사3");
		fixture.agencyLike(1L, USER_ID);
		fixture.agencyLike(2L, USER_ID);
		fixture.agencyLike(3L, OTHER_USER_ID);

		Set<Long> result = agencyLikesRepository.findLikedAgencyIds(USER_ID, List.of(1L, 3L));

		assertEquals(Set.of(1L), result);
	}

	@Test
	void findLikedReviewIds_본인이_좋아요한_대상_리뷰만_반환() {
		fixture.building(1L, "B-1", "건물1", "ROOM", null);
		fixture.generalReview(11L, USER_ID, 1L, null, T);
		fixture.generalReview(12L, USER_ID, 1L, null, T);
		fixture.generalReview(13L, USER_ID, 1L, null, T);
		fixture.reviewLike(11L, USER_ID);
		fixture.reviewLike(12L, USER_ID);
		fixture.reviewLike(13L, OTHER_USER_ID);

		Set<Long> result = reviewLikesRepository.findLikedReviewIds(USER_ID, List.of(11L, 13L));

		assertEquals(Set.of(11L), result);
	}
}
