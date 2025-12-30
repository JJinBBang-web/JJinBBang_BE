package JJinBBang.app.domain.common.service;

import JJinBBang.app.domain.common.dto.request.ReportRequest;
import JJinBBang.app.domain.common.entity.Reports;
import JJinBBang.app.domain.common.enums.ReportCategory;
import JJinBBang.app.domain.common.exception.ReportNotFoundGroupException;
import JJinBBang.app.domain.common.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportAdminServiceImpl implements ReportAdminService {

    private final ReportRepository reportRepository;

    @Transactional
    @Override
    public void createReport(ReportRequest req) {
        Reports report = Reports.create(
                req.coverImage(),
                ReportCategory.from(req.category()),
                req.title(),
                req.content()
        );

        reportRepository.save(report);
    }

    @Transactional
    @Override
    public void updateReport(Long reportId, ReportRequest req) {
        Reports report = reportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundGroupException::reportNotFound);

        report.update(
                ReportCategory.from(req.category()),
                req.coverImage(),
                req.title(),
                req.content()
        );
    }

    @Transactional
    @Override
    public void deleteReport(Long reportId) {
        Reports report = reportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundGroupException::reportNotFound);

        reportRepository.delete(report);
    }
}
