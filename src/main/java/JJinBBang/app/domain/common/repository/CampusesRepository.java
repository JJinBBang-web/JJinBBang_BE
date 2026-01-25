package JJinBBang.app.domain.common.repository;

import JJinBBang.app.domain.common.entity.Campuses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("CommonCampusesRepository")
public interface CampusesRepository extends JpaRepository<Campuses, Long> {
    List<Campuses> findByUniversityId(Long universityId);

    @Query(value = """
        SELECT
          c.campus_id      AS campusId,
          c.campus_name    AS campusName,
          c.campus_address AS campusAddress,
          u.university_id  AS universityId,
          u.university_name AS universityName
        FROM campuses c
        JOIN universities u ON u.university_id = c.university_id
        WHERE REPLACE(CONCAT(u.university_name, c.campus_name), ' ', '')
              LIKE CONCAT('%', :kw, '%')
        ORDER BY
          -- 1) 접두사 우선(결합 문자열 기준)
          CASE
            WHEN REPLACE(CONCAT(u.university_name, c.campus_name), ' ', '')
                 LIKE CONCAT(:kw, '%') THEN 0
            ELSE 1
          END ASC,
        
          -- 2) 키워드가 앞에서 발견될수록
          LOCATE(
            :kw,
            REPLACE(CONCAT(u.university_name, c.campus_name), ' ', '')
          ) ASC,
        
          -- 3) 짧은 것 우선
          (CHAR_LENGTH(u.university_name) + CHAR_LENGTH(c.campus_name)) ASC,
        
          -- 4) 안정 정렬
          u.university_name ASC,
          c.campus_name ASC,
          c.campus_id ASC
        LIMIT :limit OFFSET :offset;
        """, nativeQuery = true)
    List<CampusSearchRow> search(
        @Param("kw") String kw,
        @Param("limit") int limit,
        @Param("offset") int offset
    );
}
