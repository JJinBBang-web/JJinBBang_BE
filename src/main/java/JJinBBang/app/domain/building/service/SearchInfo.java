package JJinBBang.app.domain.building.service;

import JJinBBang.app.domain.building.dto.InfoDto;
import JJinBBang.app.domain.building.entity.*;
import JJinBBang.app.domain.building.enums.ReviewType;
import JJinBBang.app.domain.building.exception.BookmarkNotFoundException;
import JJinBBang.app.domain.building.repository.*;
import JJinBBang.app.domain.common.entity.Campuses;
import JJinBBang.app.domain.common.entity.Universities;
import JJinBBang.app.global.error.exception.UnprocessableGroupException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchInfo {
    private final BuildingsRepository buildingsRepository;
    private final ReviewsRepository reviewsRepository;
    private final AgenciesRepository agenciesRepository;
    private final GeneralReviewsRepository generalReviewsRepository;
    private final DormReviewsRepository dormReviewsRepository;

    public InfoDto reviewSearch(Long reviewId, Boolean liked){
        Reviews item = reviewsRepository.findById(reviewId).orElseThrow(() -> new BookmarkNotFoundException("Review"));
        switch (item.getDtype()){
            case ReviewType.GENERAL:
                GeneralReviews generalReviews = generalReviewsRepository.findById(reviewId).orElseThrow(()-> new BookmarkNotFoundException("GeneralReview"));
                Buildings buildings = item.getBuilding();
                return InfoDto.ofGeneralReviewInfo(generalReviews, buildings,liked);
            case ReviewType.DORM:
                DormReviews dormReviews =dormReviewsRepository.findById(reviewId).orElseThrow(()-> new BookmarkNotFoundException("DormReview"));
                Buildings buildings1 = item.getBuilding();
                Campuses campuses = buildings1.getCampus();
                Universities universities = campuses.getUniversity();
                String universityName = universities.getUniversityName();
                return InfoDto.ofDormitoryReviewInfo(dormReviews, buildings1, universityName,liked);
            case ReviewType.AGENCY:
                Agencies agencies = item.getAgency();
                return InfoDto.ofAgencyReviewInfo(item,agencies,liked);
            default:
                throw new UnprocessableGroupException("후기 유형 dtype에 이상있습니다.");
        }
    }

    public InfoDto buildingSearch(Long itemId, Boolean liked){
        Buildings building = buildingsRepository.findById(itemId).orElseThrow(() -> new BookmarkNotFoundException("Building"));
        Reviews reviews= reviewsRepository.findFirstByBuildingOrderByCreatedAtDesc(building);
        if (building.getBuildingType().equals("DORMITORY")){
            Campuses campuses = building.getCampus();
            Universities universities = campuses.getUniversity();
            String universityName = universities.getUniversityName();
            return InfoDto.ofDormitoryBuildingInfo(reviews,building,universityName,liked);
        }
        else{
            return InfoDto.ofGeneralBuildingInfo(reviews,building,liked);
        }
    }

    public InfoDto agencySearch(Long itemId, Boolean liked){
        Agencies agencies = agenciesRepository.findById(itemId).orElseThrow(() -> new BookmarkNotFoundException("Agencies"));
        Reviews reviews = reviewsRepository.findFirstByAgencyAndDtypeOrderByCreatedAtDesc(agencies,"AGENCY");
        return InfoDto.ofAgencyReviewInfo(reviews,agencies,liked);
    }
}
