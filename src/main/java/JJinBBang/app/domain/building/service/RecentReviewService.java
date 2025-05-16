package JJinBBang.app.domain.building.service;

import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.common.dto.InfoDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RecentReviewService {
    List<InfoDto> findRecentReviews(List<Long> reviewIds, Users users);
}
