package JJinBBang.app.domain.common.dto.item;

import java.util.List;

import JJinBBang.app.domain.building.enums.BuildingType;
import JJinBBang.app.domain.building.enums.ContractType;
import JJinBBang.app.global.common.enums.KeywordType;
import JJinBBang.app.global.common.enums.ViewType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record Filters(
	@NotNull(message = "{filter.viewType.notNull}")
	@Pattern(regexp = "^(ALL|BUILDING|REVIEW)$", message = "{filter.viewType.invalid}")
	ViewType viewType, // 보기 유형

	@NotNull(message = "{filter.buildType.notNull}")
	@Pattern(regexp = "^(ALL|ROOM|HOUSE|OFFICETEL|APARTMENT|DORMITORY|BOARDING_HOUSE|AGENCY)$", message = "{filter.buildType.invalid}")
	@Size(min = 1, message = "buildType 리스트는 최소 1개의 값을 포함해야 합니다.")
	List<BuildingType> buildType, // 건물 유형

	@NotNull(message = "{filter.contractType.notNull}")
	String contractType, // 계약 유형 (ALL, MONTHLY_RENT, DEPOSIT_RENT)

	List<Long> campus, // 캠퍼스 ID 목록

	@Min(value = 0, message = "{filter.depositMin.min}")
	Integer depositMin, // 보증금 최소

	Integer depositMax, // 보증금 최대, 최대 값일경우 null로 설정

	@Min(value = 0, message = "{filter.monthlyRentMin.min}")
	Integer monthlyRentMin, // 월세 최소

	Integer monthlyRentMax, // 월세 최대, 최대 값일경우 null로 설정

	Boolean inMaintenanceCost, // 관리비 포함 여부, null일 경우 관리비 포함 여부 무시

	@Max(value = 5, message = "{keyword.max}")
	// 키워드 검증 필요할듯 => 커스텀 어노테이션으로
	List<KeywordType> reviewKeyword // 리뷰 키워드
) {
}
