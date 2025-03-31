package JJinBBang.app.domain.building.service;

import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.domain.building.repository.ReviewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecentReviewServiceImpl implements RecentReviewService {

    private final ReviewsRepository reviewsRepository;

    @Override
    @Transactional
    public List<Reviews> findRecentReviews(List<Long> reviewIds) {
        return reviewsRepository.findByIdIn(reviewIds);
    }
}
