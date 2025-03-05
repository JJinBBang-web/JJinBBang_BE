package JJinBBang.app.domain.building.service;

import JJinBBang.app.domain.building.entity.BuildingLikes;
import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.entity.ReviewLikes;
import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.domain.building.exception.BuildingLikesNotFoundException;
import JJinBBang.app.domain.building.exception.BuildingsNotFoundException;
import JJinBBang.app.domain.building.exception.ReviewLikesNotFoundException;
import JJinBBang.app.domain.building.exception.ReviewsNotFoundException;
import JJinBBang.app.domain.building.repository.BuildingLikesRepository;
import JJinBBang.app.domain.building.repository.BuildingsRepository;
import JJinBBang.app.domain.building.repository.ReviewLikesRepository;
import JJinBBang.app.domain.building.repository.ReviewsRepository;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.exception.UserNotFoundException;
import JJinBBang.app.domain.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkServiceImpl implements BookmarkService{

    private final UsersRepository usersRepository;
    private final BuildingsRepository buildingsRepository;
    private final BuildingLikesRepository buildingLikesRepository;
    private final ReviewsRepository reviewsRepository;
    private final ReviewLikesRepository reviewLikesRepository;

    @Override
    @Transactional
    public void BuildingBookmark(Long buildingId, Long userId, boolean liked){
        Buildings buildings = buildingsRepository.findById(buildingId).orElseThrow(() -> new BuildingsNotFoundException("해당 Building이 존재하지 않습니다."));
        Users  users = usersRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("해당 User가 존재하지 않습니다."));
        if(liked){
            BuildingLikes newLike = BuildingLikes.create(buildings, users);
            buildingLikesRepository.save(newLike);
        }
        else{
            BuildingLikes savedLike = buildingLikesRepository.findByBuildingAndUser(buildings,users).orElseThrow(() -> new BuildingLikesNotFoundException("해당 BuildingLikes이 존재하지 않습니다."));
            buildingLikesRepository.delete(savedLike);
        }
    }

    @Override
    @Transactional
    public void ReviewBookmark(Long reviewId, Long userId, boolean liked) {
        Reviews reviews = reviewsRepository.findById(reviewId).orElseThrow(() -> new ReviewsNotFoundException("해당 Review이 존재하지 않습니다."));
        Users  users = usersRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("해당 User가 존재하지 않습니다."));
        if(liked){
            ReviewLikes newLike = ReviewLikes.create(reviews, users);
            reviewLikesRepository.save(newLike);
        }
        else{
            ReviewLikes savedLike = reviewLikesRepository.findByReviewAndUser(reviews,users).orElseThrow(() -> new ReviewLikesNotFoundException("해당 ReviewLikes이 존재하지 않습니다."));
            reviewLikesRepository.delete(savedLike);
        }

    }
}
