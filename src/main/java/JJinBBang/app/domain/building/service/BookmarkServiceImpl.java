package JJinBBang.app.domain.building.service;

import JJinBBang.app.domain.building.dto.GetUserBookmarkRequest;
import JJinBBang.app.domain.building.dto.GetUserBookmarkResponse;
import JJinBBang.app.domain.building.entity.*;
import JJinBBang.app.domain.building.exception.*;
import JJinBBang.app.domain.building.repository.*;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.exception.UserNotFoundException;
import JJinBBang.app.domain.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkServiceImpl implements BookmarkService{

    private final UsersRepository usersRepository;
    private final BuildingsRepository buildingsRepository;
    private final BuildingLikesRepository buildingLikesRepository;
    private final ReviewsRepository reviewsRepository;
    private final ReviewLikesRepository reviewLikesRepository;
    private final AgenciesRepository agenciesRepository;
    private final AgencyLikesRepository agencyLikesRepository;
    private final BookmarkRepository bookmarkRepository;

    @Override
    @Transactional
    public void BuildingBookmark(Long buildingId, Long userId, boolean liked){
        Buildings buildings = buildingsRepository.findById(buildingId).orElseThrow(() -> new BookmarkNotFoundException("해당 Building이 존재하지 않습니다."));
        Users  users = usersRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("해당 User가 존재하지 않습니다."));
        if(liked){
            BuildingLikes newLike = BuildingLikes.create(buildings, users);
            buildingLikesRepository.save(newLike);
        }
        else{
            BuildingLikes savedLike = buildingLikesRepository.findByBuildingAndUser(buildings,users).orElseThrow(() -> new BookmarkNotFoundException("해당 BuildingLikes이 존재하지 않습니다."));
            buildingLikesRepository.delete(savedLike);
        }
    }

    @Override
    @Transactional
    public void ReviewBookmark(Long reviewId, Long userId, boolean liked) {
        Reviews reviews = reviewsRepository.findById(reviewId).orElseThrow(() -> new BookmarkNotFoundException("해당 Review이 존재하지 않습니다."));
        Users  users = usersRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("해당 User가 존재하지 않습니다."));
        if(liked){
            ReviewLikes newLike = ReviewLikes.create(reviews, users);
            reviewLikesRepository.save(newLike);
        }
        else{
            ReviewLikes savedLike = reviewLikesRepository.findByReviewAndUser(reviews,users).orElseThrow(() -> new BookmarkNotFoundException("해당 ReviewLikes이 존재하지 않습니다."));
            reviewLikesRepository.delete(savedLike);
        }

    }

    @Override
    @Transactional
    public void AgencyBookmark(Long agencyId, Long userId, boolean liked) {
        Agencies agencies = agenciesRepository.findById(agencyId).orElseThrow(()->new BookmarkNotFoundException("해당 Agencies이 존재하지 않습니다."));
        Users  users = usersRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("해당 User가 존재하지 않습니다."));
        if(liked){
            AgencyLikes newLike = AgencyLikes.create(agencies, users);
            agencyLikesRepository.save(newLike);
        }
        else{
            AgencyLikes saveLike = agencyLikesRepository.findByAgencyAndUser(agencies,users).orElseThrow(() -> new BookmarkNotFoundException("해당 AgencyLikes이 존재하지 않습니다."));
            agencyLikesRepository.delete(saveLike);
        }
    }

    @Override
    @Transactional
    public Page<Object[]> SearchBookmark(Long userId, Pageable pageable , GetUserBookmarkRequest request) {
        Page<Object[]> resultPage = bookmarkRepository.findLikedItemsByUserIdNative(userId, pageable,request);
        List<Object[]> resultList = new ArrayList<>();
        List<Object[]> contentList = resultPage.getContent();
        for (Object[] row : contentList) {
            Long itemId = ((Number) row[0]).longValue();
            String itemType = (String) row[1];
            switch (itemType){
                case "review":
                    Reviews item = reviewsRepository.findById(itemId).orElseThrow(() -> new BookmarkNotFoundException("해당 Review이 존재하지 않습니다."));

                    break;
                case "building":
                    break;
                case "agency":
                    break;
            }
        }

        return resultPage;
    }

    private GetUserBookmarkResponse ReviewSearch(Long reviewId){
        Reviews item = reviewsRepository.findById(reviewId).orElseThrow(() -> new BookmarkNotFoundException("해당 Review이 존재하지 않습니다."));



        return null;
    }
}
