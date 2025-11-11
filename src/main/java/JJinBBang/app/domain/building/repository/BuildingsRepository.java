package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.repository.custom.BuildingsRepositoryCustom;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildingsRepository extends JpaRepository<Buildings, Long>, BuildingsRepositoryCustom {
    Optional<Buildings> findByBuildingCode(String buildingCode);

    @Modifying(clearAutomatically = true) // 쿼리 실행 후 영속성 컨텍스트를 클리어하여 데이터 불일치 방지
    @Query("UPDATE Buildings r SET r.likeCount = r.likeCount + 1 WHERE r.id = :buildingId")
    void incrementLikeCount(@Param("buildingId") Long buildingId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Buildings r SET r.likeCount = r.likeCount - 1 WHERE r.id = :buildingId")
    void decrementLikeCount(@Param("buildingId") Long buildingId);
}
