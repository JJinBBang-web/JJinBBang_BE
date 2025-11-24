package JJinBBang.app.domain.common.service;

import JJinBBang.app.domain.common.dto.response.ReportInfoResponse;
import JJinBBang.app.domain.common.dto.response.ReportListResponse;
import JJinBBang.app.domain.user.entity.Users;
import org.springframework.transaction.annotation.Transactional;

public interface ReportService {
    ReportListResponse getReportList(String category, Long cursor, int size);

    @Transactional
    ReportInfoResponse getReportDetail(Users user, Long reportId);

    @Transactional
    void addLike(Users user, Long reportId);

    @Transactional
    void deleteLike(Users user, Long reportId);

    @Transactional
    void addShareCount(Long reportId);
}
