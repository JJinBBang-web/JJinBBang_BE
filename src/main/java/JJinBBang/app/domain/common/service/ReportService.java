package JJinBBang.app.domain.common.service;

import JJinBBang.app.domain.common.dto.response.ReportListResponse;

public interface ReportService {
    ReportListResponse getReportList(String category, int cursor, int size);
}
