package JJinBBang.app.global.common.dto;

import java.util.List;

import JJinBBang.app.global.common.enums.KeywordType;
import jakarta.validation.constraints.NotNull;

public record Keywords (
	@NotNull
	List<KeywordType> positive,
	@NotNull
	List<KeywordType> negative
) {
}
