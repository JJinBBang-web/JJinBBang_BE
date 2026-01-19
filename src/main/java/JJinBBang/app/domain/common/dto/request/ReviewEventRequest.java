package JJinBBang.app.domain.common.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewEventRequest(
        @Valid
        @NotNull
        EventReview review,

        @NotBlank(message = "전화번호를 입력하지 않으면 이벤트 참여가 어렵습니다.")
        String phoneNumber,

        @AssertTrue(message = "이벤트 참여 및 마케팅에 동의해야 합니다.")
        boolean hasAgreedToMarketing,  // 마케팅 동의

        @AssertTrue(message = "개인정보 수집에 동의해야 합니다.")
        boolean hasAgreedToPrivacy  // 개인정보 동의
) {
}
