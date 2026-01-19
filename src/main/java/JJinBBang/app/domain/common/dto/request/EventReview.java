package JJinBBang.app.domain.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public record EventReview(
        @NotBlank(message = "재학 중인 대학교를 입력해주세요.")
        String university,

        @NotBlank(message = "계약 형태를 입력해주세요.")
        String contractType,  // 계약 형태

        @PositiveOrZero(message = "보증금의 최소 금액은 0원입니다.")
        int deposit,  // 보증금

        @PositiveOrZero(message = "월세의 최소 금액은 0원입니다.")
        int monthlyRent,  // 월세

        @PositiveOrZero(message = "관리비의 최소 금액은 0원입니다.")
        int administrationCost,  // 관리비

        @NotNull
        @Size(min = 3, message = "장점은 최소 3개 이상 선택해야 합니다.")
        List<String> positiveKeywords,

        @NotNull
        @Size(min = 3, message = "단점은 최소 3개 이상 선택해야 합니다.")
        List<String> negativeKeywords,

        @Size(max = 3, message = "이미지는 최대 3장까지 업로드 가능합니다.")
        List<String> images,

        @NotBlank
        @Size(min = 20, message = "리뷰는 최소 20자 이상 작성해야 합니다.")
        String content
) {
}
