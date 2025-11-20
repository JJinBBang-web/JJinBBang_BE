package JJinBBang.app.domain.common.entity;

import JJinBBang.app.domain.user.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportLikes {

    @EmbeddedId
    private ReportLikeId id;

    @MapsId("reportId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Reports report; // target: 찐빵 리포트

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user; // target: 사용자

    public static ReportLikes create(Reports report, Users user) {

        ReportLikeId reportLikeId = ReportLikeId.of(report.getId(), user.getUserId());

        ReportLikes reportLikes = new ReportLikes();
        reportLikes.id = reportLikeId;
        reportLikes.report = report;
        reportLikes.user = user;

        report.getReportLikes().add(reportLikes);

        return reportLikes;
    }
}
