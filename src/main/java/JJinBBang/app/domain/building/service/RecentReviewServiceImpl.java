package JJinBBang.app.domain.building.service;

import JJinBBang.app.domain.building.dto.InfoDto;
import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.domain.building.repository.ReviewLikesRepository;
import JJinBBang.app.domain.building.repository.ReviewsRepository;
import JJinBBang.app.domain.user.entity.Users;
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
    private  final ReviewLikesRepository reviewLikesRepository;

    @Override
    @Transactional
    public List<InfoDto> findRecentReviews(List<Long> reviewIds, Users users) {
        List<InfoDto> resultList = new ArrayList<>();
        for (Long reviewId : reviewIds) {
            try {
                Boolean liked=reviewLikesRepository.findByReviewIdAndUserUserId(reviewId, users.getUserId()).isPresent();

                resultList.add(searchInfo.reviewSearch(reviewId,liked));
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }
        return resultList;
    }
}
