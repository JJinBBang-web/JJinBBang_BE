package JJinBBang.app.domain.building.dto;

import JJinBBang.app.global.common.enums.KeywordType;

public record KeywordCount(
	KeywordType key,
	Integer count
) {
}
