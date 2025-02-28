package JJinBBang.app.domain.building.dto;

import JJinBBang.app.global.common.enums.KeywordType;

public record Keyword(
	KeywordType key,
	Integer count
) {
}
