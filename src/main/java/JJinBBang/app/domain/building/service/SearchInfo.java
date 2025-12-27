package JJinBBang.app.domain.building.service;

import JJinBBang.app.domain.building.enums.BuildingType;
import JJinBBang.app.global.common.dto.InfoDto;
import JJinBBang.app.domain.building.entity.*;
import JJinBBang.app.domain.building.enums.ReviewType;
import JJinBBang.app.domain.building.exception.BookmarkNotFoundException;
import JJinBBang.app.domain.building.repository.*;
import JJinBBang.app.domain.common.entity.Campuses;
import JJinBBang.app.domain.common.entity.Universities;
import JJinBBang.app.global.common.dto.SearchInfoDto;
import JJinBBang.app.global.common.enums.ViewType;
import JJinBBang.app.domain.building.exception.ReviewInternalServerErrorException;
import JJinBBang.app.global.error.exception.UnprocessableGroupException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SearchInfo {
    private final BuildingsRepository buildingsRepository;
    private final ReviewsRepository reviewsRepository;
    private final AgenciesRepository agenciesRepository;
    private final GeneralReviewsRepository generalReviewsRepository;
    private final DormReviewsRepository dormReviewsRepository;
    private final ReviewDetailsRepository reviewDetailRepository;

    public InfoDto reviewSearch(Long reviewId, Boolean liked) {
        Reviews item = reviewsRepository.findById(reviewId).orElseThrow(() -> new BookmarkNotFoundException("Review"));
        switch (item.getDtype()) {
            case ReviewType.GENERAL:
                GeneralReviews generalReviews = generalReviewsRepository.findById(reviewId)
                        .orElseThrow(() -> new BookmarkNotFoundException("GeneralReview"));
                if (generalReviews.getContractType() == null || generalReviews.getFloor() == null
                        || generalReviews.getArea() == null) {
                    throw new BookmarkNotFoundException("GeneralReview");
                }
                return InfoDto.ofGeneralReviewInfo(generalReviews, liked, getImageCount(item));
            case ReviewType.DORM:
                DormReviews dormReviews = dormReviewsRepository.findById(reviewId)
                        .orElseThrow(() -> new BookmarkNotFoundException("DormReview"));
                if (dormReviews.getDormFee() == null || dormReviews.getDormFee() == null
                        || dormReviews.getCapacity() == null) {
                    throw new BookmarkNotFoundException("DormReview");
                }

                Buildings buildings1 = item.getBuilding();
                Campuses campuses = buildings1.getCampus();
                Universities universities = campuses.getUniversity();
                String universityName = universities.getUniversityName();
                return InfoDto.ofDormitoryReviewInfo(dormReviews, buildings1, universityName, liked,
                        getImageCount(item));
            case ReviewType.AGENCY:
                Agencies agencies = item.getAgency();
                return InfoDto.ofAgencyReviewInfo(item, agencies, liked, getImageCount(item));
            default:
                throw new UnprocessableGroupException("후기 유형 dtype에 이상있습니다.");
        }
    }

    public InfoDto buildingSearch(Long itemId, Boolean liked) {
        Buildings building = buildingsRepository.findById(itemId)
                .orElseThrow(() -> new BookmarkNotFoundException("Building"));
        Reviews reviews = reviewsRepository.findFirstByBuildingOrderByCreatedAtDesc(building);
        List<BuildingType> typeList = building.getBuildingType();

        if (typeList.equals(List.of(BuildingType.DORMITORY))) {
            Campuses campuses = building.getCampus();
            Universities universities = campuses.getUniversity();
            String universityName = universities.getUniversityName();
            return InfoDto.ofDormitoryBuildingInfo(reviews, building, universityName, liked, getImageCount(reviews));
        } else {
            return InfoDto.ofGeneralBuildingInfo(reviews, building, liked, getImageCount(reviews));
        }
    }

    public InfoDto agencySearch(Long itemId, Boolean liked) {
        Agencies agencies = agenciesRepository.findById(itemId)
                .orElseThrow(() -> new BookmarkNotFoundException("Agencies"));
        Reviews reviews = reviewsRepository.findFirstByAgencyAndDtypeOrderByCreatedAtDesc(agencies, ReviewType.AGENCY);
        return InfoDto.ofAgencyBuildingInfo(reviews, agencies, liked, getImageCount(reviews));
    }

    public SearchInfoDto reviewSearchWithBound(Long reviewId, Boolean liked) {
        Reviews item = reviewsRepository.findById(reviewId).orElseThrow(() -> new BookmarkNotFoundException("Review"));
        switch (item.getDtype()) {
            case ReviewType.GENERAL:
                GeneralReviews generalReviews = generalReviewsRepository.findById(reviewId)
                        .orElseThrow(() -> new BookmarkNotFoundException("GeneralReview"));
                if (generalReviews.getContractType() == null || generalReviews.getFloor() == null
                        || generalReviews.getArea() == null) {
                    throw new BookmarkNotFoundException("GeneralReview");
                }
                return SearchInfoDto.ofSearchGeneralReviewInfo(generalReviews, liked);
            case ReviewType.DORM:
                DormReviews dormReviews = dormReviewsRepository.findById(reviewId)
                        .orElseThrow(() -> new BookmarkNotFoundException("DormReview"));
                if (dormReviews.getDormFee() == null || dormReviews.getDormFee() == null
                        || dormReviews.getCapacity() == null) {
                    throw new BookmarkNotFoundException("DormReview");
                }
                Buildings building = item.getBuilding();
                Campuses campuses = building.getCampus();
                Universities universities = campuses.getUniversity();
                String universityName = universities.getUniversityName();
                return SearchInfoDto.ofSearchDormitoryReviewInfo(dormReviews, building, universityName, liked);
            case ReviewType.AGENCY:
                Agencies agencies = item.getAgency();
                return SearchInfoDto.ofSearchAgencyReviewInfo(item, agencies, liked);
            default:
                throw new UnprocessableGroupException("후기 유형 dtype에 이상있습니다.");
        }
    }

    public SearchInfoDto buildingSearchWithBound(Long itemId, Boolean liked) {
        Buildings building = buildingsRepository.findById(itemId)
                .orElseThrow(() -> new BookmarkNotFoundException("Building"));

        Reviews reviews = reviewsRepository.findFirstByBuildingOrderByCreatedAtDesc(building);

        BuildingType mainType = BuildingType.ALL;
        if (building.getBuildingType().contains(BuildingType.DORMITORY)) {
            mainType = BuildingType.DORMITORY;
        } else if (building.getBuildingType().contains(BuildingType.AGENCY)) {
            mainType = BuildingType.AGENCY;
        }

        switch (mainType) {
            case DORMITORY:
                Campuses campuses = building.getCampus();
                Universities universities = campuses.getUniversity();
                String universityName = universities.getUniversityName();
                return SearchInfoDto.ofSearchDormitoryBuildingInfo(reviews, building, universityName, liked);
            case AGENCY:
                Agencies agency = agenciesRepository.findByAgencySerial(building.getBuildingCode())
                        .orElseThrow(() -> new BookmarkNotFoundException("Agencies"));
                Reviews agencyReview = reviewsRepository.findFirstByAgencyAndDtypeOrderByCreatedAtDesc(
                        agency, ReviewType.AGENCY);
                return SearchInfoDto.ofSearchAgencyBuildingInfo(agencyReview, agency, liked);
            default:
                return SearchInfoDto.ofSearchGeneralBuildingInfo(reviews, building, liked);
        }
    }

    public SearchInfoDto agencySearchWithBound(Long itemId, Boolean liked, ViewType viewType) {
        Agencies agencies = agenciesRepository.findById(itemId)
                .orElseThrow(() -> new BookmarkNotFoundException("Agencies"));
        Reviews reviews = reviewsRepository
                .findFirstByAgencyAndDtypeOrderByCreatedAtDesc(agencies, ReviewType.AGENCY);
        if (viewType == ViewType.REVIEW) {
            if (reviews == null) {
                return SearchInfoDto.ofSearchAgencyBuildingInfo(null, agencies, liked);
            }
            return SearchInfoDto.ofSearchAgencyReviewInfo(reviews, agencies, liked);
        }

        return SearchInfoDto.ofSearchAgencyBuildingInfo(reviews, agencies, liked);
    }

    private Integer getImageCount(Reviews review) {
        if (review == null) {
            return 0;
        }
        ReviewDetails details = reviewDetailRepository.findByReviewId(review.getId())
                .orElseThrow(ReviewInternalServerErrorException::missingReviewDetailException);
        return details.getImageCount();
    }
}
