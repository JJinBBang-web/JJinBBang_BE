package JJinBBang.app.domain.common.service;

import JJinBBang.app.domain.common.dto.request.ReportRequest;

public interface ReportAdminService {

    void createReport(ReportRequest req);

    void updateReport(Long reportId, ReportRequest req);

    void deleteReport(Long reportId);
}
