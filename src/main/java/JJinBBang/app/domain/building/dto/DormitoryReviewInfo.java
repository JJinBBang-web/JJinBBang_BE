package JJinBBang.app.domain.building.dto;

import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.entity.DormReviews;
import JJinBBang.app.domain.building.enums.Floor;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record DormitoryReviewInfo(
        Long id,
        String name,
        String type,
        String universityName,
        String floor,
        Integer capacity,
        Integer dormFee,
        BigDecimal rating,
        Boolean liked
) {
    public static DormitoryReviewInfo of(DormReviews dormReviews,Buildings building,String universityName, Boolean liked) {
        return DormitoryReviewInfo.builder()
                .id(building.getId())
                .name(building.getBuildingName())
                .type(building.getBuildingType())
                .universityName(universityName)
                .floor(dormReviews.getFloor().getDescription())
                .capacity(dormReviews.getCapacity())
                .dormFee(dormReviews.getDormFee())
                .rating(dormReviews.getRating())
                .liked(liked)
                .build();
    }
}
