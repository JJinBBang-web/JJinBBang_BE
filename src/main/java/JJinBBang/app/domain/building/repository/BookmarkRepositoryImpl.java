package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.dto.GetUserBookmarkRequest; // GetUserBookmarkRequest 임포트
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BookmarkRepositoryImpl implements BookmarkRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Object[]> findLikedItemsByUserIdNative(Long userId, Pageable pageable, GetUserBookmarkRequest request) {

        String baseDataQuery; // 기본 쿼리 문자열 (type에 따라 달라짐)
        String countQuery;    // 총 개수 쿼리 문자열 (type에 따라 달라짐)

        // request.type() 값에 따라 쿼리 문자열 동적 구성
        switch (request.type()) {
            case "review": // 후기만 조회
                baseDataQuery = """
                    SELECT
                        item_id, item_type, item_created_at, item_like_count, item_rating
                    FROM (
                        SELECT r.review_id AS item_id, 'review' AS item_type, r.created_at AS item_created_at,
                               r.likes_count AS item_like_count, r.rating AS item_rating
                        FROM reviews r JOIN review_likes rl ON r.review_id = rl.review_id WHERE rl.user_id = ?1
                    ) AS combined_liked_items
                    """;
                countQuery = """
                    SELECT COUNT(*)
                    FROM (
                        SELECT rl.review_id FROM review_likes rl WHERE rl.user_id = ?1
                    ) AS combined_count
                    """;
                break;

            case "building":
                baseDataQuery = """
                    SELECT
                        item_id, item_type, item_created_at, item_like_count, item_rating
                    FROM (
                        SELECT b.building_id AS item_id, 'building' AS item_type, b.created_at AS item_created_at,
                               b.like_count AS item_like_count, b.rating AS item_rating
                        FROM buildings b JOIN building_likes bl ON b.building_id = bl.building_id WHERE bl.user_id = ?1

                        UNION ALL

                        SELECT a.agency_id AS item_id, 'agency' AS item_type, a.created_at AS item_created_at,
                               a.like_count AS item_like_count, a.rating AS item_rating
                        FROM agencies a JOIN agency_likes al ON a.agency_id = al.agency_id WHERE al.user_id = ?1
                    ) AS combined_liked_items
                    """;
                countQuery = """
                    SELECT COUNT(*)
                    FROM (
                        SELECT bl.building_id FROM building_likes bl WHERE bl.user_id = ?1
                        UNION ALL
                        SELECT al.agency_id FROM agency_likes al WHERE al.user_id = ?1
                    ) AS combined_count
                    """;
                break;

            case "all": // 모든 타입 조회 (원래 쿼리)
            default: // 잘못된 type일 경우 기본값으로 all 처리
                baseDataQuery = """
                    SELECT
                        item_id, item_type, item_created_at, item_like_count, item_rating
                    FROM (
                        SELECT r.review_id AS item_id, 'review' AS item_type, r.created_at AS item_created_at,
                               r.likes_count AS item_like_count, r.rating AS item_rating
                        FROM reviews r JOIN review_likes rl ON r.review_id = rl.review_id WHERE rl.user_id = ?1

                        UNION ALL

                        SELECT b.building_id AS item_id, 'building' AS item_type, b.created_at AS item_created_at,
                               b.like_count AS item_like_count, b.rating AS item_rating
                        FROM buildings b JOIN building_likes bl ON b.building_id = bl.building_id WHERE bl.user_id = ?1

                        UNION ALL

                        SELECT a.agency_id AS item_id, 'agency' AS item_type, a.created_at AS item_created_at,
                               a.like_count AS item_like_count, a.rating AS item_rating
                        FROM agencies a JOIN agency_likes al ON a.agency_id = al.agency_id WHERE al.user_id = ?1

                    ) AS combined_liked_items
                    """;
                countQuery = """
                    SELECT COUNT(*)
                    FROM (
                        SELECT rl.review_id FROM review_likes rl WHERE rl.user_id = ?1
                        UNION ALL
                        SELECT bl.building_id FROM building_likes bl WHERE bl.user_id = ?1
                        UNION ALL
                        SELECT al.agency_id FROM agency_likes al WHERE al.user_id = ?1
                    ) AS combined_count
                    """;
                break;
        }

        // 정렬 기준에 따라 ORDER BY 절을 동적으로 추가 (request.sortBy() 사용)
        String orderByClause;

        switch (request.sortBy()) {
            case "latest":
                orderByClause = " ORDER BY item_created_at DESC";
                break;
            case "likes":
                orderByClause = " ORDER BY item_like_count DESC";
                break;
            case "stars":
                orderByClause = " ORDER BY item_rating DESC"; // NULLS LAST 등 필요시 추가
                break;
            default: // 정의되지 않은 sortBy일 경우 기본 정렬
                orderByClause = " ORDER BY item_created_at DESC";
        }


        // 최종 dataQuery 문자열 완성
        String finalDataQuery = baseDataQuery + orderByClause;


        // 1. 데이터 조회 쿼리 실행
        Query dataJpaQuery = entityManager.createNativeQuery(finalDataQuery); // 동적으로 생성된 쿼리 사용
        dataJpaQuery.setParameter(1, userId); // 위치 기반 파라미터 설정

        // 페이지네이션 적용 (LIMIT, OFFSET)
        dataJpaQuery.setFirstResult((int) pageable.getOffset()); // OFFSET
        dataJpaQuery.setMaxResults(pageable.getPageSize()); // LIMIT

        @SuppressWarnings("unchecked")
        List<Object[]> content = dataJpaQuery.getResultList();

        // 2. 총 개수 쿼리 실행
        Query countJpaQuery = entityManager.createNativeQuery(countQuery); // type에 따라 달라지는 countQuery 사용
        countJpaQuery.setParameter(1, userId); // 위치 기반 파라미터 설정

        Long total = ((Number) countJpaQuery.getSingleResult()).longValue();

        // 3. Page 객체 생성하여 반환
        return new PageImpl<>(content, pageable, total);
    }
}
