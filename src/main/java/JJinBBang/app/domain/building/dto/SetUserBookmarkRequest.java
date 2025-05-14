package JJinBBang.app.domain.building.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


public record SetUserBookmarkRequest(
        @NotBlank(message = "type은 필수 입력값이며 비어 있을 수 없습니다.")
        @Pattern(regexp = "^(review|building|agency)$", message = "타입은 'review' 또는 'building'만 가능합니다.")
        String type,
        @NotNull(message = "id은 필수 입력값이며 비어 있을 수 없습니다.")
        Long id,
        @NotNull(message = "bookmark은 필수 입력값이며 비어 있을 수 없습니다.")
        Boolean bookmark
) {

}
