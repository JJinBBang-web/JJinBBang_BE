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
@Builder(toBuilder = true)
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

	/**
	 * 긍정 키워드 리스트를 받아 각 키워드에 대한 카운트를 1씩 증가시킵니다.
	 * 기존 카운트가 없으면 새롭게 추가하며, 있으면 기존 객체를 교체하여 업데이트합니다.
	 *
	 * @param positives 증가시킬 긍정 키워드 리스트
	 */
	public void incrementPositiveKeywords(List<KeywordType> positives) {
		for (KeywordType keyword : positives) {
			// 기존에 해당 키워드가 있는지 조회
			Optional<KeywordCount> existing = keywordCounts.stream()
				.filter(kc -> kc.key().equals(keyword))
				.findFirst();

			if (existing.isPresent()) {
				KeywordCount old = existing.get();
				int idx = keywordCounts.indexOf(old);
				// 기존 키워드의 count를 +1 한 새 객체로 교체
				keywordCounts.set(idx, new KeywordCount(keyword, old.count() + 1));
			} else {
				// 새 키워드는 count=1로 초기화하여 추가
				keywordCounts.add(new KeywordCount(keyword, 1));
			}
		}
	}

	/**
	 * 긍정 키워드 리스트를 받아 각 키워드에 대한 카운트를 1씩 감소시킵니다.
	 * 카운트는 0 미만으로 감소하지 않도록 방어 처리합니다.
	 *
	 * @param positives 감소시킬 긍정 키워드 리스트
	 */
	public void decrementPositiveKeywords(List<KeywordType> positives) {
		for (KeywordType keyword : positives) {
			keywordCounts.stream()
					.filter(keywordCount -> keywordCount.key().equals(keyword))
					.findFirst()
					.ifPresent(old -> {
						int idx = keywordCounts.indexOf(old);
						// 0 미만으로 내려가지 않도록 최대 0으로 설정
						int newCnt = Math.max(0, old.count() - 1);
						keywordCounts.set(idx, new KeywordCount(keyword, newCnt));
					});
		}
	}
}
