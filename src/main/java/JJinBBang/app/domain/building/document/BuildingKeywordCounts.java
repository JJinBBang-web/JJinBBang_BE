package JJinBBang.app.domain.building.document;

import java.util.List;

import lombok.Builder;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import JJinBBang.app.domain.building.dto.KeywordCount;
import lombok.Getter;

@Document(collection = "building_keyword_counts")
@Getter
@Builder
public class BuildingKeywordCounts {
	@Id
	private ObjectId id;

	@Field("building_id")
	private Long buildingId;

	@Field("is_agency")
	private Boolean isAgency;

	@Field("keywordCounts")
	private List<KeywordCount> keywordCounts;

	public static BuildingKeywordCounts of(Long buildingId) {
		return BuildingKeywordCounts.builder()
				.buildingId(buildingId)
				.build();
	}
}