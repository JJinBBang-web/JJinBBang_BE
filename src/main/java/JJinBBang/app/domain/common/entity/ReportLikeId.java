package JJinBBang.app.domain.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportLikeId implements Serializable {

    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "user_id")
    private Long userId;

    // 복합키 생성
    public static ReportLikeId of(Long reportId, Long userId) {
        if (reportId == null || userId == null)
            throw new IllegalArgumentException("reportId and userId can not be null");

        return new ReportLikeId(reportId, userId);
    }
}
