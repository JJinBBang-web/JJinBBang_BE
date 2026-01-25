package JJinBBang.app.domain.common.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record CampusSearchResponse(
	List<CampusSearchItem> items,
	int limit,
	int offset
) {

	public static CampusSearchResponse of(
		List<CampusSearchItem> items,
		int limit,
		int offset
	) {
		return CampusSearchResponse.builder()
			.items(items)
			.limit(limit)
			.offset(offset)
			.build();
	}

	@Builder
	public record CampusSearchItem (
		String fullName,
		String campusAddress,
		Long campusId,
		String campusName,
		Long universityId,
		String universityName
	) {
		public static CampusSearchItem of(
			String fullName,
			String campusAddress,
			Long campusId,
			String campusName,
			Long universityId,
			String universityName
		) {
			return CampusSearchItem.builder()
				.fullName(fullName)
				.campusAddress(campusAddress)
				.campusId(campusId)
				.campusName(campusName)
				.universityId(universityId)
				.universityName(universityName)
				.build();
		}
	}
}
