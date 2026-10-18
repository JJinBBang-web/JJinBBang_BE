package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.domain.building.enums.BuildingType;
import JJinBBang.app.domain.building.enums.ReviewType;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ReviewsRepository extends CrudRepository<Reviews,Long> {
    Reviews findFirstByBuildingOrderByCreatedAtDesc(Buildings building);
    Reviews findFirstByAgencyAndDtypeOrderByCreatedAtDesc(Agencies agency, ReviewType dtype);

    /**
     * 건물별 가장 최근(createdAt) 리뷰를 조회합니다.
     * {@link #findFirstByBuildingOrderByCreatedAtDesc}의 배치 버전입니다.
     */
    @Query("SELECT r FROM Reviews r WHERE r.building.id IN :buildingIds"
        + " AND r.createdAt = (SELECT MAX(r2.createdAt) FROM Reviews r2 WHERE r2.building = r.building)")
    List<Reviews> findLatestByBuildingIds(@Param("buildingIds") Collection<Long> buildingIds);

    /**
     * 중개사별 가장 최근(createdAt) 리뷰를 조회합니다.
     * {@link #findFirstByAgencyAndDtypeOrderByCreatedAtDesc}의 배치 버전입니다.
     */
    @Query("SELECT r FROM Reviews r WHERE r.agency.agencyId IN :agencyIds AND r.dtype = :dtype"
        + " AND r.createdAt = (SELECT MAX(r2.createdAt) FROM Reviews r2 WHERE r2.agency = r.agency AND r2.dtype = :dtype)")
    List<Reviews> findLatestByAgencyIdsAndDtype(@Param("agencyIds") Collection<Long> agencyIds,
        @Param("dtype") ReviewType dtype);
    Page<Reviews> findAllByBuilding(Buildings building, Pageable pageable);
    List<Reviews> findAllByBuilding(Buildings building);
    Page<Reviews> findAllByAgency(Agencies agency, Pageable pageable);

    @Query("SELECT DISTINCT r.buildingType FROM Reviews r WHERE r.building = :building")
    Set<BuildingType> findDistinctBuildingTypesByBuilding(Buildings building);

    @Modifying(clearAutomatically = true) // 쿼리 실행 후 영속성 컨텍스트를 클리어하여 데이터 불일치 방지
    @Query("UPDATE Reviews r SET r.likesCount = r.likesCount + 1 WHERE r.id = :reviewId")
    void incrementLikeCount(@Param("reviewId") Long reviewId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Reviews r SET r.likesCount = r.likesCount - 1 WHERE r.id = :reviewId")
    void decrementLikeCount(@Param("reviewId") Long reviewId);
}
