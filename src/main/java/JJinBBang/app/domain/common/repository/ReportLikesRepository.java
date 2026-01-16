package JJinBBang.app.domain.common.repository;

import JJinBBang.app.domain.common.entity.ReportLikeId;
import JJinBBang.app.domain.common.entity.ReportLikes;
import JJinBBang.app.domain.common.entity.Reports;
import JJinBBang.app.domain.user.entity.Users;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportLikesRepository extends JpaRepository<ReportLikes, ReportLikeId> {
    boolean existsByReportAndUser(Reports report, Users user);

    @EntityGraph(attributePaths = {"report"})
    List<ReportLikes> findAllByUser_UserId(Long userId);

    @Query("SELECT rl.report.id FROM ReportLikes rl WHERE rl.user = :user AND rl.report IN :reports")
    List<Long> findLikedReportIds(@Param("user") Users users, @Param("reports") List<Reports> reports);
}
