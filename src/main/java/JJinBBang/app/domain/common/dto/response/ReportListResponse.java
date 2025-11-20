package JJinBBang.app.domain.common.dto.response;

import JJinBBang.app.global.common.dto.CursorPaginationInfo;
import JJinBBang.app.global.common.dto.ReportInfo;

import java.util.List;

public record ReportListResponse(
        List<ReportInfo> reportList,
        CursorPaginationInfo pageInfo
) {
}
