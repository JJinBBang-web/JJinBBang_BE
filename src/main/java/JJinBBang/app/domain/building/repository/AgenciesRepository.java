package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.repository.custom.AgenciesRepositoryCustom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AgenciesRepository extends JpaRepository<Agencies, Long>, AgenciesRepositoryCustom {
    Optional<Agencies> findByAgencySerial(String agencySerial);

    @Modifying(clearAutomatically = true) // 쿼리 실행 후 영속성 컨텍스트를 클리어하여 데이터 불일치 방지
    @Query("UPDATE Agencies r SET r.likeCount = r.likeCount + 1 WHERE r.agencyId = :agencyId")
    void incrementLikeCount(@Param("agencyId") Long agencyId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Agencies r SET r.likeCount = r.likeCount - 1 WHERE r.agencyId = :agencyId")
    void decrementLikeCount(@Param("agencyId") Long agencyId);
}
