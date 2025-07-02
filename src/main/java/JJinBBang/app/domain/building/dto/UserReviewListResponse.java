package JJinBBang.app.domain.building.dto;

import java.util.List;

public record UserReviewListResponse(
        List<UserReviewResponse> reviews
) {
}