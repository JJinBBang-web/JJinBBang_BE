package JJinBBang.app.domain.building.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import JJinBBang.app.domain.building.dto.*;
import JJinBBang.app.domain.building.exception.*;
import JJinBBang.app.domain.building.repository.*;
import JJinBBang.app.domain.common.entity.Campuses;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import JJinBBang.app.domain.building.document.BuildingKeywordCounts;
import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.AgencyReviews;
import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.dto.ReviewDetailResponse;
import JJinBBang.app.domain.building.entity.DormReviews;
import JJinBBang.app.domain.building.entity.DormitoryFacilities;
import JJinBBang.app.domain.building.entity.Facilities;
import JJinBBang.app.domain.building.entity.GeneralReviews;
import JJinBBang.app.domain.building.entity.ReviewDetails;
import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.domain.building.enums.BuildingType;
import JJinBBang.app.domain.building.enums.ReviewType;
import JJinBBang.app.domain.building.enums.UsageType;
import JJinBBang.app.domain.common.dto.PaginatedResponse;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.common.enums.KeywordType;
import lombok.RequiredArgsConstructor;

/**
 * ReviewServiceImpl는 리뷰 생성, 조회, 수정 기능을 제공하는 서비스 구현체입니다.
 * - GENERAL, DORM, AGENCY 타입별 리뷰 처리
 * - 건물 및 공인중개사 엔티티 조회 또는 생성
 * - 평점, 이미지 카운트, 키워드 통계 업데이트
 */
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewsRepository reviewsRepository;
    private final ReviewDetailsRepository reviewDetailsRepository;
    private final BuildingsRepository buildingsRepository;
    private final AgenciesRepository agenciesRepository;
    private final FacilitiesRepository facilitiesRepository;
    private final DormitoryFacilitiesRepository dormitoryFacilitiesRepository;
    private final BuildingKeywordCountsRepository buildingKeywordCountsRepository;
    private final CampusesRepository campusesRepository;

    /**
     * 특정 건물 또는 공인중개사에 대한 리뷰 목록을 페이징 조회
     * @param buildingId 건물 또는 공인중개사 ID
     * @param isAgency true면 공인중개사, false면 건물
     * @param user 조회 요청 사용자(좋아요 여부 확인용)
     * @param pageRequest 페이징 및 정렬 정보
     * @return ReviewSummaryResponse 페이징 응답
     */
    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ReviewSummaryResponse> getReviewList(
            Long buildingId,
            Boolean isAgency,
            Users user,
            PageRequest pageRequest
    ) {
        // 1) 조회 대상 엔티티 로드
        Page<Reviews> reviewPage;
        if (isAgency) {
            Agencies agency = agenciesRepository.findById(buildingId)
                    .orElseThrow(BuildingNullException::new);  // 공인중개사 없으면 예외

            // 2) 공인중개사 리뷰 페치
            reviewPage = reviewsRepository.findAllByAgency(agency, pageRequest);
        } else {
            Buildings building = buildingsRepository.findById(buildingId)
                    .orElseThrow(BuildingNullException::new);  // 건물 없으면 예외

            // 2) 건물 리뷰 페치
            reviewPage = reviewsRepository.findAllByBuilding(building, pageRequest);
        }

        // 3) 각 리뷰를 ReviewSummaryResponse로 변환하여 반환
        return PaginatedResponse.of(
                reviewPage,
                review -> {
                    // 3.1) 좋아요 여부 확인
                    boolean liked = review.getReviewLikes().stream()
                            .anyMatch(like -> like.getUser().equals(user));

                    // 3.2) 리뷰 상세 정보 로드
                    ReviewDetails detail = reviewDetailsRepository.findByReviewId(review.getId())
                            .orElseThrow(ReviewInternalServerErrorException::missingReviewDetailException);

                    // 3.3) 요약 응답 생성
                    return ReviewSummaryResponse.of(review, liked, detail);
                }
        );
    }

    /**
     * 단일 리뷰 상세 조회
     * @param reviewId 조회할 리뷰 ID
     * @param user 조회 요청 사용자(좋아요 여부 확인용)
     * @return ReviewDetailResponse 상세 응답
     */
    @Override
    @Transactional(readOnly = true)
    public ReviewDetailResponse getReviewDetail(Long reviewId, Users user) {
        // 1) 리뷰 엔티티 로드
        Reviews review = reviewsRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::missingReview);

        // 2) 좋아요 여부 확인
        boolean liked = review.getReviewLikes().stream()
                .anyMatch(like -> like.getUser().equals(user));

        // 3) 상세 정보 로드
        ReviewDetails detail = reviewDetailsRepository.findByReviewId(reviewId)
                .orElseThrow(ReviewInternalServerErrorException::missingReviewDetailException);

        // 4) 리뷰 타입에 따라 적절한 DTO로 변환
        if (review instanceof GeneralReviews general) {
            return ReviewDetailResponse.ofGeneral(general, detail, liked);
        } else if (review instanceof DormReviews dorm) {
            return ReviewDetailResponse.ofDormitory(dorm, detail, liked);
        } else if (review instanceof AgencyReviews agency) {
            return ReviewDetailResponse.ofAgency(agency, detail, liked);
        } else {
            // 지원하지 않는 타입이면 서버 오류
            throw ReviewInternalServerErrorException.notSupportReviewType();
        }
    }

    /**
     * 리뷰 생성 진입점
     * @param reviewRequest DTO로 전달된 리뷰 생성 정보
     * @param user 작성자 엔티티
     * @param reviewType 리뷰 타입(GENERAL/DORM/AGENCY)
     * @return CreateReviewResponse 생성된 리뷰 ID
     */
    @Override
    @Transactional
    public CreateReviewResponse createReview(
            ReviewRequest reviewRequest,
            Users user,
            ReviewType reviewType
    ) {
        // 리뷰 타입에 따라 분기 처리
        if (reviewType == ReviewType.GENERAL) {
            return CreateReviewResponse.from(createGeneralReview(reviewRequest, user));
        } else if (reviewType == ReviewType.DORM) {
            return CreateReviewResponse.from(createDormitoryReview(reviewRequest, user));
        } else if (reviewType == ReviewType.AGENCY) {
            return CreateReviewResponse.from(createAgencyReview(reviewRequest, user));
        } else {
            // 미지원 타입 예외
            throw ReviewInternalServerErrorException.notSupportReviewType();
        }
    }

    /**
     * 일반 리뷰 생성 로직
     * 1) 건물 조회 또는 생성
     * 2) GeneralReviews 엔티티 저장
     * 3) ReviewDetails 저장
     * 4) 키워드 통계, 평점, 이미지 카운트 업데이트
     */
    private Long createGeneralReview(ReviewRequest dto, Users user) {
        // 1) 건물 로드/생성
        Buildings building = findOrCreateBuilding(dto.buildingRequest(), null);

        // 2) GeneralReviews 엔티티 변환 및 저장
        GeneralReviews review = dto.toGeneralReviews(user, building);
        GeneralReviews saved = reviewsRepository.save(review);

        // 3) ReviewDetails 엔티티 저장
        ReviewDetails details = dto.toReviewDetails(saved.getId(), building.getId());
        reviewDetailsRepository.save(details);

        // 4.1) 키워드 통계 증가
        updateKeywordCounts(
                building.getId(), false,
                Collections.emptyList(), dto.keywords().positive()
        );

        // 4.2) 평점 반영
        building.addRating(saved.getRating());

        // 4.3) 이미지 카운트 반영
        if (!dto.imageUrls().isEmpty()) {
            building.incrementImagesCount();
        }

        // 4.4) 건물 엔티티 저장
        buildingsRepository.save(building);

        return saved.getId();
    }

    /**
     * 기숙사 리뷰 생성 로직
     * 1) 캠퍼스 조회
     * 2) 건물 조회 또는 생성
     * 3) DormReviews 저장
     * 4) DormitoryFacilities 목록 저장
     * 5) ReviewDetails 저장
     * 6) 키워드 통계, 평점, 이미지 업데이트
     */
    private Long createDormitoryReview(ReviewRequest dto, Users user) {
        // 1) 캠퍼스 조회
        Campuses campus = campusesRepository.findByCampusName(dto.dormitoryReview().getCampus())
                .orElseThrow(CampusNotFoundException::missingCampus);

        // 2) 건물 로드/생성
        Buildings building = findOrCreateBuilding(dto.buildingRequest(), campus);


        // 3) DormReviews 엔티티 저장
        DormReviews review = dto.toDormReviews(user, building);
        DormReviews saved = reviewsRepository.save(review);


        // 4) 시설 엔티티 리스트 생성 및 저장
        List<DormitoryFacilities> facilityList = convertToDormitoryFacilityList(dto.facilities(), saved);
        dormitoryFacilitiesRepository.saveAll(facilityList);


        // 5) ReviewDetails 엔티티 저장
        ReviewDetails details = dto.toReviewDetails(saved.getId(), building.getId());
        reviewDetailsRepository.save(details);

        // 6.1) 키워드 통계 증가
        updateKeywordCounts(building.getId(), false, Collections.emptyList(), dto.keywords().positive());

        // 6.2) 평점 반영
        building.addRating(saved.getRating());

        // 6.3) 이미지 카운트 반영
        if (!dto.imageUrls().isEmpty()) {
            building.incrementImagesCount();
        }

        // 6.4) 건물 엔티티 저장
        buildingsRepository.save(building);

        return saved.getId();
    }

    /**
     * 공인중개사 리뷰 생성 로직
     * 1) 공인중개사 조회 또는 생성
     * 2) AgencyReviews 저장
     * 3) ReviewDetails 저장
     * 4) 키워드 통계, 평점, 이미지 업데이트
     */
    private Long createAgencyReview(ReviewRequest dto, Users user) {
        // 1) 공인중개사 로드/생성
        Agencies agency = findOrCreateAgency(dto.buildingRequest());

        // 2) AgencyReviews 엔티티 저장
        AgencyReviews review = dto.toAgencyReviews(user, agency);
        AgencyReviews saved = reviewsRepository.save(review);

        // 3) ReviewDetails 엔티티 저장
        ReviewDetails details = dto.toReviewDetails(saved.getId(), agency.getAgencyId());
        reviewDetailsRepository.save(details);

        // 4.1) 키워드 통계 증가
        updateKeywordCounts(agency.getAgencyId(), true, Collections.emptyList(), dto.keywords().positive());

        // 4.2) 평점 반영
        agency.addRating(saved.getRating());

        // 4.3) 이미지 카운트 반영
        if (!dto.imageUrls().isEmpty()) {
            agency.incrementImagesCount();
        }

        // 4.4) 공인중개사 엔티티 저장
        agenciesRepository.save(agency);

        return saved.getId();
    }

    /**
     * 건물 조회 또는 신규 생성 헬퍼
     * @param dto BuildingRequest DTO
     * @param campus 소속 캠퍼스(기숙사 리뷰용)
     * @return Buildings 엔티티
     */
    private Buildings findOrCreateBuilding(BuildingRequest dto, Campuses campus) {
        return buildingsRepository.findByBuildingCode(dto.buildingCode())
                .map(existing -> {
                    // 기존 타입 목록에 새로운 BuildingType 추가
                    List<BuildingType> types = new ArrayList<>(existing.getBuildingType());
                    if (!types.contains(dto.type())) {
                        types.add(dto.type());
                        existing.setBuildingType(types);
                        buildingsRepository.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    // 신규 건물 생성 및 초기 키워드 통계 생성
                    Buildings created = buildingsRepository.save(dto.toBuildingEntity(campus));
                    BuildingKeywordCounts counts = BuildingKeywordCounts.of(created.getId(), false);
                    buildingKeywordCountsRepository.save(counts);
                    return created;
                });
    }

    /**
     * 공인중개사 조회 또는 신규 생성 헬퍼
     * @param dto BuildingRequest DTO
     * @return Agencies 엔티티
     */
    private Agencies findOrCreateAgency(BuildingRequest dto) {
        return agenciesRepository.findByBuildingCode(dto.buildingCode())
                .orElseGet(() -> {
                    // 신규 공인중개사 생성 및 초기 키워드 통계 생성
                    Agencies created = agenciesRepository.save(dto.toAgencyEntity());
                    BuildingKeywordCounts counts = BuildingKeywordCounts.of(created.getAgencyId(), true);
                    buildingKeywordCountsRepository.save(counts);
                    return created;
                });
    }

    /**
     * 키워드 통계 업데이트 헬퍼
     * @param buildingId 건물 또는 공인중개사 ID
     * @param isAgency true=공인중개사, false=건물
     * @param oldPos 이전 긍정 키워드 목록
     * @param newPos 신규 긍정 키워드 목록
     */
    private void updateKeywordCounts(
            Long buildingId,
            Boolean isAgency,
            List<KeywordType> oldPos,
            List<KeywordType> newPos
    ) {
        // 1) BuildingKeywordCounts 조회 또는 신규 생성
        BuildingKeywordCounts counts = buildingKeywordCountsRepository
                .findByBuildingIdAndIsAgency(buildingId, isAgency)
                .orElseGet(() -> BuildingKeywordCounts.of(buildingId, isAgency));

        // 2) 이전 키워드 감소
        counts.decrementPositiveKeywords(oldPos == null ? Collections.emptyList() : oldPos);

        // 3) 신규 키워드 증가
        counts.incrementPositiveKeywords(newPos);

        // 4) 저장
        buildingKeywordCountsRepository.save(counts);
    }

    /**
     * 기숙사 시설 변환 헬퍼
     * @param dto FacilitiesDto
     * @param review DormReviews 엔티티
     * @return DormitoryFacilities 리스트
     */
    private List<DormitoryFacilities> convertToDormitoryFacilityList(FacilitiesDto dto, DormReviews review) {
        List<DormitoryFacilities> list = new ArrayList<>();
        // 공용 시설 항목 생성
        for (String name : dto.publicFacilities()) {
            Facilities f = facilitiesRepository.findByName(name)
                    .orElseThrow(BuildingNotFoundException::unsupportedDormitoryFacility);
            list.add(DormitoryFacilities.create(review, f, true, UsageType.PUBLIC));
        }

        // 전용 시설 항목 생성
        for (String name : dto.privateFacilities()) {
            Facilities f = facilitiesRepository.findByName(name)
                    .orElseThrow(BuildingNotFoundException::unsupportedDormitoryFacility);
            list.add(DormitoryFacilities.create(review, f, true, UsageType.PRIVATE));
        }

        // 라운지 옵션 생성
        Facilities lounge = facilitiesRepository.findByName("lounge")
                .orElseThrow(BuildingNotFoundException::unsupportedDormitoryFacility);
        list.add(DormitoryFacilities.create(review, lounge, dto.lounge(), null));
        return list;
    }

    /**
     * 리뷰 수정 진입점
     * @param dto 업데이트할 리뷰 정보
     * @param user 수정 요청 사용자
     * @param reviewId 수정 대상 리뷰 ID
     */
    @Override
    @Transactional
    public void updateReview(ReviewRequest dto, Users user, Long reviewId) {
        // 1) 리뷰 로드 및 존재 확인
        Reviews review = reviewsRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::missingReview);

        // 2) 작성자 권한 확인
        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw ReviewAccessDeniedException.onlyAuthorCanEdit();
        }

        // 3) 리뷰 타입별 분기 처리
        if (review instanceof GeneralReviews general) {
            updateGeneralReview(general, dto);
        } else if (review instanceof DormReviews dorm) {
            updateDormitoryReview(dorm, dto);
        } else if (review instanceof AgencyReviews agency) {
            updateAgencyReview(agency, dto);
        } else {
            throw new UnsupportedOperationException("지원하지 않는 리뷰 타입입니다.");
        }
    }

    /**
     * 일반 리뷰 업데이트 로직:
     * 1) 기존/신규 건물 로드
     * 2) 기존 상세 정보 로드
     * 3) 건물 변경 시 평점·이미지·키워드 이동, 동일 건물 시 업데이트
     * 4) 리뷰 엔티티 및 상세 정보 저장
     */
    private void updateGeneralReview(GeneralReviews oldReview, ReviewRequest dto) {
        // 1) 기존/신규 건물 엔티티 준비
        Buildings oldBuilding = oldReview.getBuilding();
        Buildings newBuilding = findOrCreateBuilding(dto.buildingRequest(), null);
        ReviewDetails oldDetails = reviewDetailsRepository.findByReviewId(oldReview.getId())
                .orElseThrow(ReviewInternalServerErrorException::missingReviewDetailException);

        // 2) 건물 변경 여부 판단
        if (!oldBuilding.getBuildingCode().equals(newBuilding.getBuildingCode())) {
            // a) 이전 건물: 평점 제거, 이미지 감소, 키워드 통계 감소
            oldBuilding.removeRating(oldReview.getRating());
            oldBuilding.decrementImagesCount();
            updateKeywordCounts(oldBuilding.getId(), false, oldDetails.getKeywords().positive(), Collections.emptyList());

            // b) 신규 건물: 평점 추가, 이미지 증가, 키워드 통계 증가
            newBuilding.addRating(dto.generalReview().getRating());
            newBuilding.incrementImagesCount();
            updateKeywordCounts(newBuilding.getId(), false, Collections.emptyList(), dto.keywords().positive());

            // c) 두 건물 저장
            buildingsRepository.saveAll(List.of(oldBuilding, newBuilding));
        } else {

            // 동일 건물: 평점 및 키워드 통계 업데이트
            oldBuilding.updateRating(oldReview.getRating(), dto.generalReview().getRating());
            updateKeywordCounts(oldBuilding.getId(), false, oldDetails.getKeywords().positive(), dto.keywords().positive());
            buildingsRepository.save(oldBuilding);
        }

        // 3) 리뷰 엔티티 및 상세 정보 갱신 저장
        GeneralReviews updatedReview = dto.toUpdatedGeneralReviews(oldReview, newBuilding);
        reviewsRepository.save(updatedReview);
        ReviewDetails updatedDetails = dto.toUpdatedReviewDetails(oldDetails, newBuilding.getId());
        reviewDetailsRepository.save(updatedDetails);
    }

    /**
     * 기숙사 리뷰 업데이트 로직:
     * 1) 캠퍼스 검증 및 조회
     * 2) 기존/신규 건물 엔티티 로드
     * 3) 기존 상세 정보 로드
     * 4) 건물 변경 시 평점·이미지·키워드 이동, 동일 건물 시 업데이트
     * 5) 리뷰 엔티티 갱신 저장
     * 6) 기존 시설 삭제 후 재생성과 저장
     * 7) 상세 정보 갱신 저장
     */
    private void updateDormitoryReview(DormReviews oldReview, ReviewRequest dto) {
        // 1) 캠퍼스 검증
        Campuses campus = campusesRepository.findByCampusName(dto.dormitoryReview().getCampus())
                .orElseThrow(CampusNotFoundException::missingCampus);

        // 2) 건물 엔티티 로드
        Buildings oldBuilding = oldReview.getBuilding();
        Buildings newBuilding = findOrCreateBuilding(dto.buildingRequest(), campus);

        // 3) 기존 상세 정보 로드
        ReviewDetails oldDetails = reviewDetailsRepository.findByReviewId(oldReview.getId())
                .orElseThrow(ReviewInternalServerErrorException::missingReviewDetailException);

        // 4) 건물 변경 처리
        if (!oldBuilding.getBuildingCode().equals(newBuilding.getBuildingCode())) {
            // a) 이전 건물 통계 감소
            oldBuilding.removeRating(oldReview.getRating());
            oldBuilding.decrementImagesCount();
            updateKeywordCounts(oldBuilding.getId(), false, oldDetails.getKeywords().positive(), Collections.emptyList());

            // b) 신규 건물 통계 증가
            newBuilding.addRating(dto.dormitoryReview().getRating());
            newBuilding.incrementImagesCount();
            updateKeywordCounts(newBuilding.getId(), false, Collections.emptyList(), dto.keywords().positive());
            buildingsRepository.saveAll(List.of(oldBuilding, newBuilding));
        } else {
            // 동일 건물: 평점·키워드 업데이트
            oldBuilding.updateRating(oldReview.getRating(), dto.dormitoryReview().getRating());
            updateKeywordCounts(oldBuilding.getId(), false, oldDetails.getKeywords().positive(), dto.keywords().positive());
            buildingsRepository.save(oldBuilding);
        }

        // 5) 리뷰 엔티티 갱신 저장
        DormReviews updatedReview = dto.toUpdatedDormitoryReviews(oldReview, newBuilding);
        reviewsRepository.save(updatedReview);

        // 6) 시설 정보 재설정
        dormitoryFacilitiesRepository.deleteAllByDormitoryReview(oldReview);
        List<DormitoryFacilities> newFacilities = convertToDormitoryFacilityList(dto.facilities(), updatedReview);
        dormitoryFacilitiesRepository.saveAll(newFacilities);

        // 7) 상세 정보 갱신 저장
        ReviewDetails updatedDetails = dto.toUpdatedReviewDetails(oldDetails, newBuilding.getId());
        reviewDetailsRepository.save(updatedDetails);
    }

    /**
     * 공인중개사 리뷰 업데이트 로직:
     * 1) 기존/신규 공인중개사 로드
     * 2) 기존 상세 정보 로드
     * 3) 이미지 변경 여부에 따른 카운트 조정
     * 4) 공인중개사 변경 시 평점·키워드 이동, 동일 시 업데이트
     * 5) 리뷰 엔티티 및 상세 정보 저장
     */
    private void updateAgencyReview(AgencyReviews oldReview, ReviewRequest dto) {
        // 1) 공인중개사 로드
        Agencies oldAgency = oldReview.getAgency();
        Agencies newAgency = findOrCreateAgency(dto.buildingRequest());
        // 2) 기존 상세 정보 로드
        ReviewDetails oldDetails = reviewDetailsRepository.findByReviewId(oldReview.getId())
                .orElseThrow(ReviewInternalServerErrorException::missingReviewDetailException);
        // 3) 이미지 유무 확인
        boolean oldHasImage = oldReview.getThumbnailImage() != null;
        boolean newHasImage = dto.imageUrls() != null && !dto.imageUrls().isEmpty();

        // 4) 이미지 변경 및 공인중개사 변경 처리
        if (!oldAgency.getBuildingCode().equals(newAgency.getBuildingCode())) {
            if (!oldHasImage && newHasImage) newAgency.incrementImagesCount();
            else if (oldHasImage && !newHasImage) oldAgency.decrementImagesCount();
            else if (oldHasImage && newHasImage) {
                oldAgency.decrementImagesCount();
                newAgency.incrementImagesCount();
            }
            updateKeywordCounts(oldAgency.getAgencyId(), true, oldDetails.getKeywords().positive(), Collections.emptyList());
            updateKeywordCounts(newAgency.getAgencyId(), true, Collections.emptyList(), dto.keywords().positive());
            oldAgency.removeRating(oldReview.getRating());
            newAgency.addRating(dto.agencyReview().getRating());
            agenciesRepository.saveAll(List.of(oldAgency, newAgency));
        } else {
            if (!oldHasImage && newHasImage) oldAgency.incrementImagesCount();
            else if (oldHasImage && !newHasImage) oldAgency.decrementImagesCount();
            updateKeywordCounts(oldAgency.getAgencyId(), true, oldDetails.getKeywords().positive(), dto.keywords().positive());
            oldAgency.updateRating(oldReview.getRating(), dto.agencyReview().getRating());
            agenciesRepository.save(oldAgency);
        }

        // 5) 리뷰 및 상세 정보 저장
        AgencyReviews updatedReview = dto.toUpdatedAgencyReviews(oldReview, newAgency);
        reviewsRepository.save(updatedReview);
        ReviewDetails updatedDetails = dto.toUpdatedReviewDetails(oldDetails, newAgency.getAgencyId());
        reviewDetailsRepository.save(updatedDetails);
    }
}
