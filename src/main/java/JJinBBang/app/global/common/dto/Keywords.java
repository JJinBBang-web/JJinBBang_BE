package JJinBBang.app.global.common.dto;

import java.util.List;

import JJinBBang.app.global.common.enums.KeywordType;

public record Keywords (
	List<KeywordType> positive,
	List<KeywordType> negative
) {
}
