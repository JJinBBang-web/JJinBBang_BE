package JJinBBang.app.domain.building.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Pageable;


public record GetUserBookmarkRequest(
        @NotNull(message = "type은 필수 입력값이며 비어 있을 수 없습니다.")
        @Pattern(regexp = "^(review|building|all)$", message = "타입은 'all', 'review' 또는 'building'만 가능합니다.")
        String type,

        @NotNull(message = "sortBy은 필수 입력값이며 비어 있을 수 없습니다.")
        @Pattern(regexp = "^(latest|likes|stars)$", message = "타입은 'latest', 'likes' 또는 'stars'만 가능합니다.")
        String sortBy
) {
}
