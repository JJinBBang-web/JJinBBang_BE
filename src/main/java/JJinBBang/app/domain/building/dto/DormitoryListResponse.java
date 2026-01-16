package JJinBBang.app.domain.building.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record DormitoryListResponse(
	List<DormitoryItem> dormitories
) {
	public static DormitoryListResponse of(List<DormitoryItem> dormitories) {
		return DormitoryListResponse.builder()
			.dormitories(dormitories)
			.build();
	}
}
