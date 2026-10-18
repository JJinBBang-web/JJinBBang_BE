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
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SearchInfo {
    private final BuildingsRepository buildingsRepository;
    private final ReviewsRepository reviewsRepository;
    private final AgenciesRepository agenciesRepository;
    private final GeneralReviewsRepository generalReviewsRepository;
    private final DormReviewsRepository dormReviewsRepository;
    private final ReviewDetailsRepository reviewDetailRepository;

    public InfoDto reviewSearch(Long reviewId, Boolean liked){
        Reviews item = reviewsRepository.findById(reviewId).orElseThrow(() -> new BookmarkNotFoundException("Review"));
        switch (item.getDtype()){
            case ReviewType.GENERAL:
                GeneralReviews generalReviews = generalReviewsRepository.findById(reviewId).orElseThrow(()-> new BookmarkNotFoundException("GeneralReview"));
                if(generalReviews.getContractType()==null || generalReviews.getFloor()==null||generalReviews.getArea()==null){
                    throw new BookmarkNotFoundException("GeneralReview");
                }
                return InfoDto.ofGeneralReviewInfo(generalReviews, liked, getImageCount(item));
            case ReviewType.DORM:
                DormReviews dormReviews =dormReviewsRepository.findById(reviewId).orElseThrow(()-> new BookmarkNotFoundException("DormReview"));
                if(dormReviews.getDormFee()==null||dormReviews.getDormFee()==null||dormReviews.getCapacity()==null){
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

    public InfoDto buildingSearch(Long itemId, Boolean liked){
        Buildings building = buildingsRepository.findById(itemId).orElseThrow(() -> new BookmarkNotFoundException("Building"));
        Reviews reviews= reviewsRepository.findFirstByBuildingOrderByCreatedAtDesc(building);
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

    /**
     * 지도 검색 후보 리뷰 목록을 화면 항목 목록으로 변환합니다.
     *
     * @param reviews        검색 후보 리뷰 목록
     * @param likedReviewIds 로그인 사용자가 좋아요한 리뷰 ID (비로그인이면 빈 Set)
     */
    public List<SearchInfoDto> reviewSearchWithBound(List<Reviews> reviews, Set<Long> likedReviewIds){
        return reviews.stream()
                .map(review -> reviewSearchWithBound(review, likedReviewIds.contains(review.getId())))
                .toList();
    }

    /**
     * 지도 검색 후보 건물 목록을 화면 항목 목록으로 변환합니다.
     * 건물별 최신 리뷰와 연결된 중개사는 목록 단위로 한 번에 조회합니다.
     * 중개사 건물은 건물이 아니라 연결된 중개사(buildingCode == agencySerial)의 정보로 보여줍니다.
     *
     * @param buildings        검색 후보 건물 목록
     * @param likedBuildingIds 로그인 사용자가 좋아요한 건물 ID (비로그인이면 빈 Set)
     */
    public List<SearchInfoDto> buildingSearchWithBound(List<Buildings> buildings, Set<Long> likedBuildingIds){
        Map<Long, Reviews> latestReviews = latestReviewByBuilding(buildings);
        Map<String, Agencies> linkedAgencies = agencyBySerial(buildings);
        Map<Long, Reviews> linkedAgencyReviews = latestReviewByAgency(linkedAgencies.values());

        return buildings.stream()
                .map(building -> {
                    Agencies agency = linkedAgencies.get(building.getBuildingCode());
                    Reviews agencyReview = agency == null ? null : linkedAgencyReviews.get(agency.getAgencyId());
                    return buildingSearchWithBound(building, latestReviews.get(building.getId()), agency, agencyReview,
                            likedBuildingIds.contains(building.getId()));
                })
                .toList();
    }

    /**
     * 지도 검색 후보 중개사 목록을 화면 항목 목록으로 변환합니다.
     * 중개사별 최신 리뷰는 목록 단위로 한 번에 조회합니다.
     *
     * @param agencies       검색 후보 중개사 목록
     * @param likedAgencyIds 로그인 사용자가 좋아요한 중개사 ID (비로그인이면 빈 Set)
     * @param viewType       REVIEW 면 최신 리뷰 기준, 그 외에는 중개사 기준으로 보여줍니다
     */
    public List<SearchInfoDto> agencySearchWithBound(List<Agencies> agencies, Set<Long> likedAgencyIds, ViewType viewType){
        Map<Long, Reviews> latestReviews = latestReviewByAgency(agencies);
        return agencies.stream()
                .map(agency -> agencySearchWithBound(agency, latestReviews.get(agency.getAgencyId()),
                        likedAgencyIds.contains(agency.getAgencyId()), viewType))
                .toList();
    }

    private SearchInfoDto reviewSearchWithBound(Reviews review, Boolean liked){
        Reviews item = (Reviews) Hibernate.unproxy(review);
        switch (item.getDtype()){
            case ReviewType.GENERAL:
                GeneralReviews generalReviews = (GeneralReviews) item;
                if(generalReviews.getContractType()==null || generalReviews.getFloor()==null||generalReviews.getArea()==null){
                    throw new BookmarkNotFoundException("GeneralReview");
                }
                return SearchInfoDto.ofSearchGeneralReviewInfo(generalReviews,liked);
            case ReviewType.DORM:
                DormReviews dormReviews = (DormReviews) item;
                if(dormReviews.getDormFee()==null||dormReviews.getDormFee()==null||dormReviews.getCapacity()==null){
                    throw new BookmarkNotFoundException("DormReview");
                }
                Buildings building = item.getBuilding();
                Campuses campuses = building.getCampus();
                Universities universities = campuses.getUniversity();
                String universityName = universities.getUniversityName();
                return SearchInfoDto.ofSearchDormitoryReviewInfo(dormReviews, building, universityName, liked);
            case ReviewType.AGENCY:
                Agencies agencies = item.getAgency();
                return SearchInfoDto.ofSearchAgencyReviewInfo(item,agencies,liked);
            default:
                throw new UnprocessableGroupException("후기 유형 dtype에 이상있습니다.");
        }
    }

    private SearchInfoDto buildingSearchWithBound(Buildings building, Reviews latestReview, Agencies linkedAgency,
                                                  Reviews linkedAgencyReview, Boolean liked){
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
                return SearchInfoDto.ofSearchDormitoryBuildingInfo(latestReview, building, universityName, liked);
            case AGENCY:
                if (linkedAgency == null) {
                    throw new BookmarkNotFoundException("Agencies");
                }
                return SearchInfoDto.ofSearchAgencyBuildingInfo(linkedAgencyReview, linkedAgency, liked);
            default:
                return SearchInfoDto.ofSearchGeneralBuildingInfo(latestReview, building, liked);
        }
    }

    private SearchInfoDto agencySearchWithBound(Agencies agencies, Reviews latestReview, Boolean liked, ViewType viewType){
        if (viewType == ViewType.REVIEW) {
            if (latestReview == null) {
                return SearchInfoDto.ofSearchAgencyBuildingInfo(null, agencies, liked);
            }
            return SearchInfoDto.ofSearchAgencyReviewInfo(latestReview, agencies, liked);
        }

        return SearchInfoDto.ofSearchAgencyBuildingInfo(latestReview, agencies, liked);
    }

    /**
     * 건물 ID별 가장 최근 리뷰를 조회합니다.
     * {@link ReviewsRepository#findFirstByBuildingOrderByCreatedAtDesc}의 목록 버전입니다.
     */
    private Map<Long, Reviews> latestReviewByBuilding(List<Buildings> buildings) {
        if (buildings.isEmpty()) {
            return Map.of();
        }
        List<Long> buildingIds = buildings.stream().map(Buildings::getId).toList();
        return reviewsRepository.findLatestByBuildingIds(buildingIds).stream()
                .collect(Collectors.toMap(review -> review.getBuilding().getId(), review -> review, SearchInfo::higherId));
    }

    /**
     * 중개사 ID별 가장 최근 중개사 리뷰(AGENCY)를 조회합니다.
     * {@link ReviewsRepository#findFirstByAgencyAndDtypeOrderByCreatedAtDesc}의 목록 버전입니다.
     */
    private Map<Long, Reviews> latestReviewByAgency(Collection<Agencies> agencies) {
        if (agencies.isEmpty()) {
            return Map.of();
        }
        List<Long> agencyIds = agencies.stream().map(Agencies::getAgencyId).distinct().toList();
        return reviewsRepository.findLatestByAgencyIdsAndDtype(agencyIds, ReviewType.AGENCY).stream()
                .collect(Collectors.toMap(review -> review.getAgency().getAgencyId(), review -> review, SearchInfo::higherId));
    }

    /**
     * createdAt이 같은 리뷰가 여럿일 때 ID가 큰 쪽을 선택합니다.
     * 단건 조회는 이 경우 어느 리뷰를 고를지 정해져 있지 않으므로, 목록 버전은 결과를 고정합니다.
     */
    private static Reviews higherId(Reviews a, Reviews b) {
        return a.getId() >= b.getId() ? a : b;
    }

    /**
     * 중개사 건물의 buildingCode와 일치하는 중개사를 agencySerial 기준으로 조회합니다.
     */
    private Map<String, Agencies> agencyBySerial(List<Buildings> buildings) {
        List<String> serials = buildings.stream()
                .filter(building -> building.getBuildingType().contains(BuildingType.AGENCY))
                .map(Buildings::getBuildingCode)
                .distinct()
                .toList();
        if (serials.isEmpty()) {
            return Map.of();
        }
        return agenciesRepository.findAllByAgencySerialIn(serials).stream()
                .collect(Collectors.toMap(Agencies::getAgencySerial, agency -> agency));
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
