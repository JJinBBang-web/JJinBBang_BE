package JJinBBang.app.global.common.dto;

import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.entity.DormReviews;
import JJinBBang.app.domain.building.enums.Floor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record DormitoryReviewInfo(
        Long id,
        String name,
        List<String> type,
        String universityName,
        Floor floor,
        Integer capacity,
        Integer dormFee,
        BigDecimal rating,
        Boolean liked
) {
    public static DormitoryReviewInfo of(DormReviews dormReviews,Buildings building,String universityName, Boolean liked) {
        return DormitoryReviewInfo.builder()
                .id(dormReviews.getId())
                .name(building.getBuildingName())
                .type(List.of("기숙사"))
                .universityName(universityName)
                .floor(dormReviews.getFloor())
                .capacity(dormReviews.getCapacity())
                .dormFee(dormReviews.getDormFee())
                .rating(dormReviews.getRating())
                .liked(liked)
                .build();
    }
}
