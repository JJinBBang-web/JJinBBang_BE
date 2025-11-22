package JJinBBang.app.domain.common.repository;

import JJinBBang.app.domain.common.entity.Reports;
import JJinBBang.app.domain.common.enums.ReportCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Reports, Long> {
    Page<Reports> findByCategory(ReportCategory reportCategory, Pageable pageable);

    List<Reports> findByIdLessThanOrderByIdDesc(Long idIsLessThan, Pageable pageable);

    List<Reports> findByCategoryAndIdLessThanOrderByIdDesc(ReportCategory category, Long idIsLessThan, Pageable pageable);
}
