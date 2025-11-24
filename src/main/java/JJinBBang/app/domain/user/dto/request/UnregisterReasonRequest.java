package JJinBBang.app.domain.user.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// 탈퇴 사유 옵션
public record UnregisterReasonRequest(
        @NotNull @Min(1) @Max(6) Integer option
) {
}
