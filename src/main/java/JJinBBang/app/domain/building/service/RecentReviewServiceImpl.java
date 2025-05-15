package JJinBBang.app.domain.building.service;

import JJinBBang.app.domain.building.dto.InfoDto;
import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.domain.building.repository.ReviewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecentReviewServiceImpl implements RecentReviewService {

    private final SearchInfo searchInfo;

    @Override
    @Transactional
    public List<InfoDto> findRecentReviews(List<Long> reviewIds) {
        List<InfoDto> resultList = new ArrayList<>();
        for (Long reviewId : reviewIds) {
            try {
                resultList.add(searchInfo.ReviewSearch(reviewId));
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }
        return resultList;
    }
}
