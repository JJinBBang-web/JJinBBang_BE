package JJinBBang.app.domain.building.service;

import JJinBBang.app.domain.building.entity.Reviews;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RecentReviewService {
    List<Reviews> findRecentReviews(List<Long> reviewIds);
}
