package JJinBBang.app.global.common.dto;

import JJinBBang.app.domain.building.entity.Buildings;
import java.math.BigDecimal;

import JJinBBang.app.domain.building.entity.DormReviews;
import JJinBBang.app.domain.building.enums.BuildingType;
import JJinBBang.app.domain.building.enums.Floor;
import lombok.Builder;

@Builder
public record DormitoryReviewInfo(
        Long id,
        String name,
        BuildingType type,
        Long campusId,
        Long dormitoryId,
        String universityName,
        Floor floor,
        Integer capacity,
        Integer dormFee,
        BigDecimal rating,
        Boolean liked
) {
    public static DormitoryReviewInfo of(DormReviews dormReviews,Buildings building,String universityName, Boolean liked) {

        Long campusId = dormReviews.getBuilding().getCampus().getId();
        Long dormitoryId = dormReviews.getBuilding().getId();

        return DormitoryReviewInfo.builder()
            .id(dormReviews.getId())
            .name(building.getBuildingName())
            .type(BuildingType.DORMITORY)
            .universityName(universityName)
            .campusId(campusId)
            .dormitoryId(dormitoryId)
            .floor(dormReviews.getFloor())
            .capacity(dormReviews.getCapacity())
            .dormFee(dormReviews.getDormFee())
            .rating(dormReviews.getRating())
            .liked(liked)
            .build();
    }

    public static DormitoryReviewInfo of(DormReviews dormReviews, Boolean liked) {

        String universityName = dormReviews.getBuilding().getCampus().getUniversity().getUniversityName();
        String campusName =  dormReviews.getBuilding().getCampus().getCampusName();
        Long campusId = dormReviews.getBuilding().getCampus().getId();
        Long dormitoryId = dormReviews.getBuilding().getId();

        return DormitoryReviewInfo.builder()
            .id(dormReviews.getId())
            .name(dormReviews.getBuilding().getBuildingName())
            .type(BuildingType.DORMITORY)
            .universityName(universityName + " " + campusName)
            .campusId(campusId)
            .dormitoryId(dormitoryId)
            .floor(dormReviews.getFloor())
            .capacity(dormReviews.getCapacity())
            .dormFee(dormReviews.getDormFee())
            .rating(dormReviews.getRating())
            .liked(liked)
            .build();
    }
}
