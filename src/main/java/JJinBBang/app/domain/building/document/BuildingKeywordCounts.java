package JJinBBang.app.domain.building.document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import JJinBBang.app.global.common.enums.KeywordType;
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

	public static BuildingKeywordCounts of(Long buildingId, Boolean isAgency) {
		return BuildingKeywordCounts.builder()
			.buildingId(buildingId)
			.isAgency(isAgency)
			.keywordCounts(new ArrayList<>())
			.build();
	}

	public void incrementPositiveKeywords(List<KeywordType> positives) {
		for (KeywordType keyword : positives) {
			// 기존에 있던 카운트 찾기
			Optional<KeywordCount> existing = keywordCounts.stream()
				.filter(kc -> kc.key().equals(keyword))
				.findFirst();

			if (existing.isPresent()) {
				KeywordCount old = existing.get();
				int idx = keywordCounts.indexOf(old);
				// 동일 키워드의 카운트를 +1 한 새 객체로 교체
				keywordCounts.set(idx, new KeywordCount(keyword, old.count() + 1));
			} else {
				// 새 키워드는 count=1 로 추가
				keywordCounts.add(new KeywordCount(keyword, 1));
			}
		}
	}
}
