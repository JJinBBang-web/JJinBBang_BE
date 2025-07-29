package JJinBBang.app.domain.building.dto;

import JJinBBang.app.domain.building.entity.AgencyReviews;
import JJinBBang.app.domain.building.entity.DormReviews;
import JJinBBang.app.domain.building.entity.GeneralReviews;
import JJinBBang.app.domain.building.repository.ReviewDetailsRepository;
import JJinBBang.app.domain.building.repository.ReviewLikesRepository;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.common.dto.AgencyReviewInfo;
import JJinBBang.app.global.common.dto.DormitoryReviewInfo;
import JJinBBang.app.global.common.dto.GeneralReviewInfo;
import JJinBBang.app.global.common.dto.ReviewInfo;
import lombok.Builder;

@Builder
public record UserReviewResponse(
        GeneralReviewInfo generalReviewInfo,
        DormitoryReviewInfo dormitoryReviewInfo,
        AgencyReviewInfo agencyReviewInfo,
        ReviewInfo reviewInfo,
        String image
) {
    public static UserReviewResponse fromGeneral(
            GeneralReviews generalReviews,
            Users user,
            String image,
            ReviewLikesRepository likesRepository,
            ReviewDetailsRepository reviewDetailsRepository
    ){
        boolean liked = likesRepository.findByReviewAndUser(generalReviews, user).isPresent();

        return UserReviewResponse.builder()
                .generalReviewInfo(GeneralReviewInfo.of(generalReviews, liked))
                .reviewInfo(ReviewInfo.of(generalReviews))
                .image(image)
                .build();
    }

    public static UserReviewResponse fromDormitory(
            DormReviews dormitoryReviews,
            Users user,
            String image,
            ReviewLikesRepository likesRepository
    ){
        boolean liked = likesRepository.findByReviewAndUser(dormitoryReviews, user).isPresent();

        String campusName = dormitoryReviews
                .getBuilding()
                .getCampus()
                .getCampusName();

        return UserReviewResponse.builder()
                .dormitoryReviewInfo(DormitoryReviewInfo.of(dormitoryReviews, dormitoryReviews.getBuilding(), campusName, liked))
                .reviewInfo(ReviewInfo.of(dormitoryReviews))
                .image(image)
                .build();
    }

    public static UserReviewResponse fromAgency(
            AgencyReviews agencyReviews,
            Users user,
            String image,
            ReviewLikesRepository likesRepository
    ){
        boolean liked = likesRepository.findByReviewAndUser(agencyReviews, user).isPresent();

        return UserReviewResponse.builder()
                .agencyReviewInfo(AgencyReviewInfo.of(agencyReviews, agencyReviews.getAgency(), liked))
                .reviewInfo(ReviewInfo.of(agencyReviews))
                .image(image)
                .build();
    }
}
