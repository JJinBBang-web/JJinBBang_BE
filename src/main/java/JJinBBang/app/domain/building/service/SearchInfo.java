package JJinBBang.app.domain.building.service;

import JJinBBang.app.domain.building.dto.InfoDto;
import JJinBBang.app.domain.building.entity.*;
import JJinBBang.app.domain.building.enums.ReviewType;
import JJinBBang.app.domain.building.exception.BookmarkNotFoundException;
import JJinBBang.app.domain.building.repository.*;
import JJinBBang.app.domain.common.entity.Campuses;
import JJinBBang.app.domain.common.entity.Universities;
import JJinBBang.app.domain.user.repository.UsersRepository;
import JJinBBang.app.global.error.exception.UnprocessableGroupException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SearchInfo {
    private final BuildingsRepository buildingsRepository;
    private final ReviewsRepository reviewsRepository;
    private final AgenciesRepository agenciesRepository;
    private final GeneralReviewsRepository generalReviewsRepository;
    private final DormReviewsRepository dormReviewsRepository;

    public InfoDto ReviewSearch(Long reviewId){
        Reviews item = reviewsRepository.findById(reviewId).orElseThrow(() -> new BookmarkNotFoundException("해당 Review이 존재하지 않습니다."));
        switch (item.getDtype()){
            case ReviewType.GENERAL:
                GeneralReviews generalReviews = generalReviewsRepository.findById(reviewId).orElseThrow(()-> new BookmarkNotFoundException("해당 GeneralReview이 존재하지 않습니다."));
                Buildings buildings = item.getBuilding();
                return InfoDto.ofGeneralReviewInfo(generalReviews, buildings,true);
            case ReviewType.DORM:
                DormReviews dormReviews =dormReviewsRepository.findById(reviewId).orElseThrow(()-> new BookmarkNotFoundException("해당 DormReview이 존재하지 않습니다."));
                Buildings buildings1 = item.getBuilding();
                Campuses campuses = buildings1.getCampus();
                Universities universities = campuses.getUniversity();
                String universityName = universities.getUniversityName();
                return InfoDto.ofDormitoryReviewInfo(dormReviews, buildings1, universityName,true);
            case ReviewType.AGENCY:
                Agencies agencies = item.getAgency();
                return InfoDto.ofAgencyReviewInfo(item,agencies,true);
            default:
                throw new UnprocessableGroupException("후기 유형 dtype에 이상있습니다.");
        }
    }

    public InfoDto BuildingSearch(Long itemId){
        Buildings building = buildingsRepository.findById(itemId).orElseThrow(() -> new BookmarkNotFoundException("해당 Building이 존재하지 않습니다."));
        Reviews reviews= reviewsRepository.findFirstByBuildingOrderByCreatedAtDesc(building);
        if (building.getBuildingType().equals("DORMITORY")){
            Campuses campuses = building.getCampus();
            Universities universities = campuses.getUniversity();
            String universityName = universities.getUniversityName();
            return InfoDto.ofDormitoryBuildingInfo(reviews,building,universityName,true);
        }
        else{
            return InfoDto.ofGeneralBuildingInfo(reviews,building,true);
        }
    }

    public InfoDto AgencySearch(Long itemId){
        Agencies agencies = agenciesRepository.findById(itemId).orElseThrow(() -> new BookmarkNotFoundException("해당 Agencies이 존재하지 않습니다."));
        Reviews reviews = reviewsRepository.findFirstByAgencyAndDtypeOrderByCreatedAtDesc(agencies,"AGENCY");
        return InfoDto.ofAgencyReviewInfo(reviews,agencies,true);
    }
}
