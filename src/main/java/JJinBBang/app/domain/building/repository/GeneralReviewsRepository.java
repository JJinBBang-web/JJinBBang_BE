package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.entity.GeneralReviews;
import JJinBBang.app.domain.user.entity.Users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface GeneralReviewsRepository extends JpaRepository<GeneralReviews, Long> {

    @EntityGraph(attributePaths = "building")
    Page<GeneralReviews> findByUser_UserId(Long userId, Pageable pageable);

    // 작성자 일괄 재매핑
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Reviews r SET r.user = :systemUser WHERE r.user.userId = :userId")
    int updateUserToSystemUser(Long userId, Users systemUser);
}
