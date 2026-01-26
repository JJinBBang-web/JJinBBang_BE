package JJinBBang.app.domain.common.repository;

import JJinBBang.app.domain.common.entity.Campuses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("CommonCampusesRepository")
public interface CampusesRepository extends JpaRepository<Campuses, Long> {

    double EARTH_RADIUS = 6371.0;

    List<Campuses> findByUniversityId(Long universityId);

    @Query(value = """
        SELECT
            c.campus_id AS campusId,
            u.university_id AS universityId,
            c.campus_name AS campusName,
            c.campus_address AS campusAddress,
            c.campus_lat AS campusLat,
            c.campus_lot AS campusLot,
            c.image AS image,
            u.university_name AS universityName,
            u.university_logo AS universityLogo,
            (:earthRadius * acos(cos(radians(:lat)) * cos(radians(c.campus_lat)) * cos(radians(c.campus_lot) - radians(:lng)) +
            sin(radians(:lat)) * sin(radians(c.campus_lat)))) AS distance
        FROM campuses c
        JOIN universities u ON c.university_id = u.university_id
        ORDER BY distance ASC 
        LIMIT :limitCount
        """, nativeQuery = true)
    List<Object[]> findNearestCampuses(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("earthRadius") double earthRadius,
            @Param("limitCount") int limitCount
    );

    @Query(value = """
        SELECT 
            c.campus_id AS campusId,
            u.university_id AS universityId,
            c.campus_name AS campusName,
            c.campus_address AS campusAddress,
            c.campus_lat AS campusLat,
            c.campus_lot AS campusLot,
            c.image AS image,
            u.university_name AS universityName,
            u.university_logo AS universityLogo,
            COUNT(usr.user_id) AS userCount
        FROM universities u
        JOIN campuses c ON u.university_id = c.university_id
        LEFT JOIN users usr ON u.university_id = usr.university_id
        WHERE c.campus_id = (
            SELECT MIN(sub_c.campus_id)
            FROM campuses sub_c
            WHERE sub_c.university_id = u.university_id
        )
        GROUP BY c.campus_id, u.university_id, c.campus_name, c.campus_address, c.campus_lat, c.campus_lot, c.image, u.university_name, u.university_logo
        ORDER BY userCount DESC
        LIMIT :limitCount
        """, nativeQuery = true)
    List<Object[]> findTopPopularUniversities(@Param("limitCount") int limitCount);
}
