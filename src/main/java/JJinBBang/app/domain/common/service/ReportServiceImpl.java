package JJinBBang.app.domain.common.service;

import JJinBBang.app.domain.common.dto.response.ReportInfoResponse;
import JJinBBang.app.domain.common.dto.response.ReportListResponse;
import JJinBBang.app.domain.common.entity.ReportLikeId;
import JJinBBang.app.domain.common.entity.ReportLikes;
import JJinBBang.app.domain.common.entity.Reports;
import JJinBBang.app.domain.common.enums.ReportCategory;
import JJinBBang.app.domain.common.exception.ReportInvalidException;
import JJinBBang.app.domain.common.exception.ReportNotFoundGroupException;
import JJinBBang.app.domain.common.repository.ReportLikesRepository;
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


import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public ReportListResponse getReportList(String category, Long cursor, int size, Users user) {

        if (size <= 0) throw ReportInvalidException.invalidSize();

        Pageable pageable = PageRequest.of(
                0, size + 1, Sort.by(Sort.Direction.DESC, "id")
        );

        // category 검증
        final ReportCategory reportCategory;
        if (category == null || category.isBlank() || category.equalsIgnoreCase("ALL")) {
            reportCategory = null;
        } else {
            try {
                reportCategory = ReportCategory.valueOf(category);
            } catch (IllegalArgumentException e) {
                throw ReportInvalidException.invalidCategory();
            }
        }

        List<Reports> resultList;

        // 첫 페이지
        if (cursor == null) {
            if (reportCategory == null) {
                Page<Reports> page = reportRepository.findAll(pageable);
                resultList = page.getContent();
            }  else {
                Page<Reports> page = reportRepository.findByCategory(reportCategory, pageable);
                resultList = page.getContent();
            }
        // 다음 페이지
        } else {
            if (reportCategory == null) {
                resultList = reportRepository.findByIdLessThanOrderByIdDesc(cursor, pageable);
            } else {
                resultList = reportRepository.findByCategoryAndIdLessThanOrderByIdDesc(reportCategory, cursor, pageable);
            }
        }

        // hasNext 판별
        boolean hasNext = resultList.size() > size;
        if (hasNext) resultList = resultList.subList(0, size);

        Set<Long> likedReportIds = new HashSet<>();
        if (user != null && !resultList.isEmpty()) {
            List<Long> ids = reportLikesRepository.findLikedReportIds(user, resultList);
            likedReportIds.addAll(ids);
        }

        // 리포트 데이터 매핑
        List<ReportInfo> reports = resultList.stream()
                .map(report -> {
                    return new ReportInfo(
                            report.getId(),
                            report.getCoverImage(),
                            report.getCategory(),
                            report.getTitle(),
                            report.getCreatedAt(),
                            report.getLikeCount(),
                            report.getViewCount(),
                            likedReportIds.contains(report.getId())
                    );
                })
                .toList();

        // pagination 데이터 업데이트 (마지막 요소 id 조회)
        Long nextCursor = hasNext && !resultList.isEmpty() ? resultList.getLast().getId() : null;
        CursorPaginationInfo cursorInfo = new CursorPaginationInfo(nextCursor, hasNext);

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

    /**
     * 리포트 좋아요 추가
     *
     * @param user
     * @param reportId
     */
    @Transactional
    @Override
    public void addLike(Users user, Long reportId) {

        // 리포트 조회
        Reports report = reportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundGroupException::reportNotFound);

        // 좋아요 어부 확인
        boolean isLiked = reportLikesRepository.existsByReportAndUser(report, user);
        if (isLiked) throw new IllegalArgumentException("이미 좋아요 누름");

        // 좋아요 추가
        ReportLikes reportLikes = ReportLikes.create(report, user);
        reportLikesRepository.save(reportLikes);

        // 좋아요 수 +1
        report.increaseLikeCount();
    }


    /**
     * 리포트 좋아요 제거
     *
     * @param user
     * @param reportId
     */
    @Transactional
    @Override
    public void deleteLike(Users user, Long reportId) {

        // 좋아요 ID 조회
        ReportLikeId reportLikeId = ReportLikeId.of(reportId, user.getUserId());

        // 좋아요 여부 확인
        ReportLikes reportLikes = reportLikesRepository.findById(reportLikeId)
                .orElseThrow(ReportNotFoundGroupException::reportNotFound);

        // 좋아요 삭제
        reportLikesRepository.delete(reportLikes);

        // 좋아요 수 -1
        Reports report = reportLikes.getReport();
        report.decreaseLikeCount();
    }


    /**
     * 공유수 추가
     *
     * @param reportId
     */
    @Transactional
    @Override
    public void addShareCount(Long reportId) {
        Reports report = reportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundGroupException::reportNotFound);

        report.increaseShareCount();
    }
}
