package JJinBBang.app.domain.building.service;

import JJinBBang.app.domain.building.dto.GetUserBookmarkRequest;
import JJinBBang.app.global.common.dto.InfoDto;
import JJinBBang.app.domain.building.entity.*;
import JJinBBang.app.domain.building.exception.*;
import JJinBBang.app.domain.building.repository.*;
import JJinBBang.app.domain.user.entity.Users;
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

    private final SearchInfo searchInfo;

    private final BuildingsRepository buildingsRepository;
    private final BuildingLikesRepository buildingLikesRepository;
    private final ReviewsRepository reviewsRepository;
    private final ReviewLikesRepository reviewLikesRepository;
    private final AgenciesRepository agenciesRepository;
    private final AgencyLikesRepository agencyLikesRepository;
    private final BookmarkRepository bookmarkRepository;


    @Override
    @Transactional
    public void buildingBookmark(Long buildingId, Users users, boolean liked){
        Buildings buildings = buildingsRepository.findById(buildingId).orElseThrow(() -> new BookmarkNotFoundException("Building"));

        Boolean isLiked = buildingLikesRepository.existsByBuildingAndUser(buildings, users);

        // 원래 좋아요가 없고, 현재 좋아요를 누른 경우
        if(!isLiked && liked){
            BuildingLikes newLike = BuildingLikes.create(buildings, users);
            buildings.incrementLikeCount();
            buildingLikesRepository.save(newLike);
        }

        // 원래 좋아요가 있고, 현재 좋아요를 취소한 경우
        if (isLiked && !liked){
            BuildingLikes savedLike = buildingLikesRepository.findByBuildingAndUser(buildings,users).orElseThrow(() -> new BookmarkNotFoundException("BuildingLikes"));
            buildings.decrementLikeCount();
            buildingLikesRepository.delete(savedLike);
        }

        // 원래 좋아요가 있고, 현재 좋아요를 누른 경우
        // (변경 없음)

        // 원래 좋아요가 없고, 현재 좋아요를 취소한 경우
        // (변경 없음)
    }

    @Override
    @Transactional
    public void reviewBookmark(Long reviewId, Users users, boolean liked) {
        Reviews reviews = reviewsRepository.findById(reviewId).orElseThrow(() -> new BookmarkNotFoundException("Review"));

        Boolean isLiked = reviewLikesRepository.existsByReviewAndUser(reviews, users);

        // 원래 좋아요가 없고, 현재 좋아요 누른 경우
        if(!isLiked && liked) {
            ReviewLikes newLike = ReviewLikes.create(reviews, users);
            reviews.incrementLikeCount();
            reviewLikesRepository.save(newLike);
        }

        // 원래 좋아요가 있고, 현재 좋아요 취소를 누른 경우
        if(isLiked && !liked) {
            ReviewLikes savedLike = reviewLikesRepository.findByReviewAndUser(reviews,users).orElseThrow(() -> new BookmarkNotFoundException("ReviewLikes"));
            reviews.decrementLikeCount();
            reviewLikesRepository.delete(savedLike);
        }

        // 원래 좋아요가 있고, 현재 좋아요를 누른 경우
        // (변경 없음)

        // 원래 좋아요가 없고, 현재 좋아요 취소를 누른 경우
        // (변경 없음)
    }

    @Override
    @Transactional
    public void agencyBookmark(Long agencyId, Users users, boolean liked) {
        Agencies agencies = agenciesRepository.findById(agencyId).orElseThrow(()->new BookmarkNotFoundException("Agencies"));

        Boolean isLiked = agencyLikesRepository.existsByAgencyAndUser(agencies, users);

        // 원래 좋아요가 없고, 현재 좋아요를 누른 경우
        if(!isLiked && liked){
            AgencyLikes newLike = AgencyLikes.create(agencies, users);
            agencies.incrementLikeCount();
            agencyLikesRepository.save(newLike);
        }

        // 원래 좋아요가 있고, 현재 좋아요 취소를 누른 경우
        if(isLiked && !liked){
            AgencyLikes saveLike = agencyLikesRepository.findByAgencyAndUser(agencies,users).orElseThrow(() -> new BookmarkNotFoundException("AgencyLikes"));
            agencies.decrementLikeCount();
            agencyLikesRepository.delete(saveLike);
        }

        // 원래 좋아요가 있고, 현재 좋아요를 누른 경우
        // (변경 없음)

        // 원래 좋아요가 없고, 현재 좋아요 취소를 누른 경우
        // (변경 없음)
    }

    @Override
    @Transactional
    public List<InfoDto> searchBookmark(Long userId, Pageable pageable , GetUserBookmarkRequest request) {
        Page<Object[]> resultPage = bookmarkRepository.findLikedItemsByUserIdNative(userId, pageable,request);
        List<InfoDto> resultList = new ArrayList<>();
        List<Long> errorList = new ArrayList<>();
        List<Object[]> contentList = resultPage.getContent();
        for (Object[] row : contentList) {
            Long itemId = ((Number) row[0]).longValue();
            String itemType = (String) row[1];
            try{
            switch (itemType){
                case "review":
                    resultList.add(searchInfo.reviewSearch(itemId,true));
                    break;
                case "building":
                    resultList.add(searchInfo.buildingSearch(itemId,true));
                    break;
                case "agency":
                    resultList.add(searchInfo.agencySearch(itemId,true));
                    break;
            }
            } catch (Exception e) {
                errorList.add(itemId);
            }
        }
        System.out.println(errorList);
        return resultList;
    }

}
