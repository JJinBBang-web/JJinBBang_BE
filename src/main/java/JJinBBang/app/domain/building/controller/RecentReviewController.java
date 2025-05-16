package JJinBBang.app.domain.building.controller;

import JJinBBang.app.domain.building.exception.RecentlyViewedNotFoundException;
import JJinBBang.app.domain.building.service.RecentReviewService;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.common.dto.InfoDto;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/user/recentReview")
@RequiredArgsConstructor
public class RecentReviewController {

    private final RecentReviewService recentReviewService;

    @GetMapping("")
    public ResTemplate<List<InfoDto>> getRecentlyViewedReview(@AuthenticationPrincipal Users user, @RequestParam(name = "reviewIds", required = false)  List<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            throw RecentlyViewedNotFoundException.notreviewId();
        }
        else if (reviewIds.size() >5) {
            throw RecentlyViewedNotFoundException.longreviewId();
        }
        List<InfoDto> recentReviews = recentReviewService.findRecentReviews(reviewIds,user);
        return new ResTemplate<>(HttpStatus.OK, "처리 완료",recentReviews);
    }


}
