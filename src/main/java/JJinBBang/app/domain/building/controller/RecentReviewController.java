package JJinBBang.app.domain.building.controller;

import JJinBBang.app.domain.building.dto.InfoDto;
import JJinBBang.app.domain.building.service.RecentReviewService;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    public ResTemplate<List<InfoDto>> test(@RequestParam(name = "reviewIds") List<Long> reviewIds) {
        List<InfoDto> recentReviews = recentReviewService.findRecentReviews(reviewIds);
        return new ResTemplate<>(HttpStatus.OK, "처리 완료",recentReviews);
    }


}
