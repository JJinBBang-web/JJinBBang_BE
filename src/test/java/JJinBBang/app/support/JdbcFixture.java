package JJinBBang.app.support;

import java.time.LocalDateTime;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 테스트 데이터를 JDBC 로 직접 넣습니다.
 * ID 와 createdAt 을 직접 지정해야 하는 테스트(최신 리뷰 조회 등)를 위해 JPA 감사 기능을 거치지 않습니다.
 */
public class JdbcFixture {

	private static final LocalDateTime BASE_TIME = LocalDateTime.of(2025, 1, 1, 0, 0);

	private final JdbcTemplate jdbc;

	public JdbcFixture(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public void university(long universityId) {
		jdbc.update("insert into universities (university_id, university_name, university_logo, university_domain)"
				+ " values (?, ?, ?, ?)", universityId, "대학교" + universityId, "logo.png", "univ" + universityId + ".ac.kr");
	}

	public void campus(long campusId, long universityId) {
		jdbc.update("insert into campuses (campus_id, campus_name, campus_address, campus_lat, campus_lot, image,"
				+ " university_id) values (?, ?, ?, ?, ?, null, ?)",
				campusId, "캠퍼스" + campusId, "캠퍼스 주소", 35.15, 128.10, universityId);
	}

	public void user(long userId) {
		jdbc.update("insert into users (user_id, provider, provider_id, verification_status, role, created_at,"
				+ " updated_at) values (?, 'google', ?, 'VERIFIED', 'USER', ?, ?)",
				userId, "test-user-" + userId, BASE_TIME, BASE_TIME);
	}

	/** buildingType 은 "ROOM", "DORMITORY", "AGENCY" 처럼 저장 형식 그대로 넘깁니다 */
	public void building(long buildingId, String buildingCode, String name, String buildingType, Long campusId) {
		jdbc.update("insert into buildings (building_id, building_code, building_name, building_type,"
				+ " building_address, building_lat, building_lot, rating, review_count, like_count, images_count,"
				+ " campus_id, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, null, 0, 0, 0, ?, ?, ?)",
				buildingId, buildingCode, name, buildingType, "건물 주소", 35.15, 128.10, campusId, BASE_TIME, BASE_TIME);
	}

	public void agency(long agencyId, String agencySerial, String name) {
		jdbc.update("insert into agencies (agency_id, agency_serial, name, address, agency_lat, agency_lot, rating,"
				+ " like_count, review_count, images_count, created_at, updated_at)"
				+ " values (?, ?, ?, ?, ?, ?, null, 0, 0, 0, ?, ?)",
				agencyId, agencySerial, name, "중개사 주소", 35.15, 128.10, BASE_TIME, BASE_TIME);
	}

	public void generalReview(long reviewId, long userId, Long buildingId, Long agencyId, LocalDateTime createdAt) {
		review(reviewId, "GENERAL", "ROOM", userId, buildingId, agencyId, createdAt);
		jdbc.update("insert into general_reviews (review_id, floor, area, contract_type, deposit, price,"
				+ " maintenance_cost) values (?, 'LOW', 20.0, 'MONTHLY_RENT', 500, 40, 5)", reviewId);
	}

	public void dormReview(long reviewId, long userId, long buildingId, LocalDateTime createdAt) {
		review(reviewId, "DORM", "DORMITORY", userId, buildingId, null, createdAt);
		jdbc.update("insert into dorm_reviews (review_id, floor, capacity, dorm_fee) values (?, 'LOW', 2, 150)",
				reviewId);
	}

	public void agencyReview(long reviewId, long userId, long agencyId, LocalDateTime createdAt) {
		review(reviewId, "AGENCY", "AGENCY", userId, null, agencyId, createdAt);
		jdbc.update("insert into agency_reviews (review_id) values (?)", reviewId);
	}

	public void buildingLike(long buildingId, long userId) {
		jdbc.update("insert into building_likes (building_id, user_id) values (?, ?)", buildingId, userId);
	}

	public void agencyLike(long agencyId, long userId) {
		jdbc.update("insert into agency_likes (agency_id, user_id) values (?, ?)", agencyId, userId);
	}

	public void reviewLike(long reviewId, long userId) {
		jdbc.update("insert into review_likes (review_id, user_id) values (?, ?)", reviewId, userId);
	}

	private void review(long reviewId, String dtype, String buildingType, long userId, Long buildingId, Long agencyId,
			LocalDateTime createdAt) {
		jdbc.update("insert into reviews (review_id, dtype, likes_count, thumbnail_image, content, tags, rating,"
				+ " building_type, user_id, building_id, agency_id, created_at, updated_at)"
				+ " values (?, ?, 0, ?, ?, '', null, ?, ?, ?, ?, ?, ?)",
				reviewId, dtype, "thumb-" + reviewId + ".jpg", "후기 " + reviewId, buildingType, userId, buildingId,
				agencyId, createdAt, createdAt);
	}
}
