package JJinBBang.app.domain.common.service;

import JJinBBang.app.domain.common.dto.request.ReportRequest;
import jakarta.transaction.Transactional;

public interface ReportAdminService {

    @Transactional
    void creatReport(ReportRequest req);

    @Transactional
    void updateReport(Long reportId, ReportRequest req);

    @Transactional
    void deleteReport(Long reportId);
}
