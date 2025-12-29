package JJinBBang.app.domain.common.service;

import JJinBBang.app.domain.common.dto.request.ReportRequest;
import JJinBBang.app.domain.user.entity.Users;
import jakarta.transaction.Transactional;

public interface ReportAdminService {

    @Transactional
    void creatReport(Users users, ReportRequest req);


}
