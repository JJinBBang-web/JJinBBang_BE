package JJinBBang.app.domain.common.service;

import JJinBBang.app.domain.common.dto.response.ReportInfoResponse;
import JJinBBang.app.domain.common.dto.response.ReportListResponse;
import JJinBBang.app.domain.common.entity.Reports;
import JJinBBang.app.domain.common.enums.ReportCategory;
import JJinBBang.app.domain.common.exception.ReportInvalidException;
import JJinBBang.app.domain.common.exception.ReportNotFoundGroupException;
import JJinBBang.app.domain.common.repository.ReportRepository;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.common.dto.CursorPaginationInfo;
import JJinBBang.app.global.common.dto.ReportInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ReportLikesRepository reportLikesRepository;

    /**
     * 리포트 데이터 목록 조회
     *
     * @param category 빈 경우 전체 데이터로 조회 (enum 타입으로 정의)
     * @param cursor pagination cursor
     * @param size pagination 조회 크기
     * @return
     */
    @Override
    public ReportListResponse getReportList(String category, int cursor, int size) {

        if (cursor < 0) throw ReportInvalidException.invalidCursor();
        if (size < 0) throw ReportInvalidException.invalidSize();

        Pageable pageable = PageRequest.of(
                cursor, size, Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // category 기반 리포트 데이터 조회
        Page<Reports> page;

        if (category == null || category.isBlank()) {
            page = reportRepository.findAll(pageable);
        } else {
            final ReportCategory reportCategory;

            try {
                reportCategory = ReportCategory.valueOf(category);
            } catch (IllegalArgumentException e) {
                throw ReportInvalidException.invalidCategory();
            }

            page = reportRepository.findByCategory(reportCategory, pageable);
        }

        // 리포트 데이터 매핑
        List<ReportInfo> reports = page.getContent().stream()
                .map(report -> new ReportInfo(
                        report.getId(),
                        report.getCoverImage(),
                        report.getCategory(),
                        report.getTitle(),
                        report.getCreatedAt(),
                        report.getLikeCount(),
                        report.getViewCount()
                ))
                .toList();

        // pagination 데이터 업데이트
        Integer nextCursor = page.hasNext() ? cursor + 1 : null;
        CursorPaginationInfo cursorInfo = new CursorPaginationInfo(nextCursor, page.hasNext());

        return new ReportListResponse(reports, cursorInfo);
    }


    /**
     * 리포트 상세 내용 조회
     *
     * @param user 좋아요 여부 조회를 위함
     * @param reportId 조회하고자 하는 리포트 id
     * @return
     */
    @Transactional
    @Override
    public ReportInfoResponse getReportDetail(Users user, Long reportId) {

        // 리포트 조회
        Reports report = reportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundGroupException::reportNotFound);

        // 조회수 증가
        report.increaseViewCount();

        // 좋아요 여부 확인
        boolean isLiked = user != null && reportLikesRepository.existsByReportAndUser(report, user);

        return new ReportInfoResponse(
                report.getId(),
                report.getCategory(),
                report.getTitle(),
                report.getContent(),
                report.getCreatedAt(),
                report.getLikeCount(),
                report.getViewCount(), // 증가한 조회수 반환
                report.getShareCount(),
                isLiked
        );
    }
}
