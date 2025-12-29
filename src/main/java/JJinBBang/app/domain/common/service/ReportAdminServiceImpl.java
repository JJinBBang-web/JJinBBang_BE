package JJinBBang.app.domain.common.service;

import JJinBBang.app.domain.common.dto.request.ReportRequest;
import JJinBBang.app.domain.common.entity.Reports;
import JJinBBang.app.domain.common.enums.ReportCategory;
import JJinBBang.app.domain.common.repository.ReportRepository;
import JJinBBang.app.domain.user.entity.Users;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportAdminServiceImpl implements ReportAdminService {

    private final ReportRepository reportRepository;

    @Transactional
    @Override
    public void creatReport(Users users, ReportRequest req) {
        Reports report = Reports.create(
                req.coverImage(),
                ReportCategory.from(req.category()),
                req.title(),
                req.content()
        );

        reportRepository.save(report);
    }
}
