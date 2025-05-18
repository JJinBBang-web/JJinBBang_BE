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
                .type(BuildingType.DORMITORY)
                .universityName(universityName)
                .floor(dormReviews.getFloor())
                .capacity(dormReviews.getCapacity())
                .dormFee(dormReviews.getDormFee())
                .rating(dormReviews.getRating())
                .liked(liked)
                .build();
    }
	public static DormitoryReviewInfo of(DormReviews dormitoryReview, Boolean liked) {
			return DormitoryReviewInfo.builder()
				.id(dormitoryReview.getId())
				.name(dormitoryReview.getBuilding().getBuildingName())
				.type(BuildingType.DORMITORY)
				.universityName(dormitoryReview.getBuilding().getCampus().getUniversity().getUniversityName())
				.floor(dormitoryReview.getFloor())
				.capacity(dormitoryReview.getCapacity())
				.dormFee(dormitoryReview.getDormFee())
				.rating(dormitoryReview.getRating())
				.liked(liked)
				.build();
		}
}
