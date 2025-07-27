package JJinBBang.app.global.common.dto;

import java.util.List;

import JJinBBang.app.global.common.enums.KeywordType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record Keywords (
	@NotNull
	@Size(min = 3, max = 5, message = "긍정 키워드는 3개 이상 5개 이하로 선택해야 합니다")
	List<KeywordType> positive,
	@NotNull
	@Size(min = 3, max = 5, message = "부정 키워드는 3개 이상 5개 이하로 선택해야 합니다")
	List<KeywordType> negative
) {
}
