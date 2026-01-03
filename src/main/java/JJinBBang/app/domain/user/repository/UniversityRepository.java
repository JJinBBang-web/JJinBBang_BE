package JJinBBang.app.domain.user.repository;

import JJinBBang.app.domain.common.entity.Universities;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UniversityRepository extends JpaRepository<Universities, Long> {

    @Override
    @EntityGraph(attributePaths = {"campuses"})
    Page<Universities> findAll(Pageable pageable);

    Optional<Universities> findUniversitiesByUniversityDomain(String universityDomain);
    boolean existsByUniversityDomain(String universityDomain);

    /**
     * 입력된 도메인이 대학교 도메인과 매칭되는지 확인 (와일드카드 패턴 지원)
     * 
     * 매칭 규칙:
     * 1. 정확한 매칭: universityDomain = inputDomain
     * 2. 와일드카드 매칭: universityDomain이 '*.xxx' 형태이고 inputDomain이 'xxx' 또는 '*.xxx'로 끝나는 경우
     * 3. 서브도메인 매칭: inputDomain이 universityDomain으로 끝나는 경우 (예: mail.gnu.ac.kr → gnu.ac.kr)
     * 
     * @param inputDomain 입력된 도메인 (예: "mail.gnu.ac.kr")
     * @return 매칭되는 대학교 (우선순위: 정확한 매칭 > 와일드카드 > 서브도메인)
     */
    @Query(value = """
        SELECT u.* FROM universities u
        WHERE u.university_domain IS NOT NULL
        AND (
            u.university_domain = :inputDomain
			-- 서브도메인 매칭
            OR (:inputDomain LIKE CONCAT('%.', u.university_domain))
			-- 와일드카드 매칭
            OR (u.university_domain LIKE '*.%' AND :inputDomain LIKE CONCAT('%.', SUBSTRING(u.university_domain, 3)))
        )
        ORDER BY 
            CASE 
                WHEN u.university_domain = :inputDomain THEN 1
                WHEN u.university_domain LIKE '*.%' THEN 2
                ELSE 3
            END
        LIMIT 1
        """, nativeQuery = true)
    Optional<Universities> findFirstByDomainMatching(@Param("inputDomain") String inputDomain);

    /**
     * 입력된 도메인이 대학교 도메인과 매칭되는지 확인 (와일드카드 패턴 지원)
     * 
     * @param inputDomain 입력된 도메인 (예: "mail.gnu.ac.kr")
     * @return 매칭 여부
     */
    @Query("""
        SELECT COUNT(u) > 0
        FROM Universities u
        WHERE u.universityDomain IS NOT NULL
        AND (
            u.universityDomain = :inputDomain
            OR (:inputDomain LIKE CONCAT('%.', u.universityDomain))
            OR (u.universityDomain LIKE '*.%' AND :inputDomain LIKE CONCAT('%.', SUBSTRING(u.universityDomain, 3)))
        )
        """)
    boolean existsByDomainMatching(@Param("inputDomain") String inputDomain);
}
