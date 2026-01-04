package JJinBBang.app.domain.common.repository;

import JJinBBang.app.domain.common.entity.ReportLikeId;
import JJinBBang.app.domain.common.entity.ReportLikes;
import JJinBBang.app.domain.common.entity.Reports;
import JJinBBang.app.domain.user.entity.Users;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportLikesRepository extends JpaRepository<ReportLikes, ReportLikeId> {
    boolean existsByReportAndUser(Reports report, Users user);

    @EntityGraph(attributePaths = {"report"})
    List<ReportLikes> findAllByUser_UserId(Long userId);
}
