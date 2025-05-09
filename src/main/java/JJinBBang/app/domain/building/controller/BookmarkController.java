package JJinBBang.app.domain.building.controller;

import JJinBBang.app.domain.building.dto.SetUserBookmarkRequest;
import JJinBBang.app.domain.building.exception.BodyInvalidGroupException;
import JJinBBang.app.domain.building.service.BookmarkService;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/user/bookmark")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("")
    public ResTemplate<String> postBookmarks(@AuthenticationPrincipal Users user, @RequestBody SetUserBookmarkRequest request) {
        String errorMessage = "";
        if (request.getType() == null) {
            errorMessage += "type ";
        }
        if (request.getId() == null) {
            errorMessage += "id ";
        }
        if (request.getBookmark() == null) {
            errorMessage += "bookmark ";
        }
        if (!errorMessage.isEmpty()) {
            throw new BodyInvalidGroupException(errorMessage+"요소가 누락되었습니다.");
        }
        if (request.getType().equals("building")){

            bookmarkService.BuildingBookmark(request.getId(),user.getUserId(),request.getBookmark());
        }
        else if (request.getType().equals("review")){
            // 위와 동일
            bookmarkService.ReviewBookmark(request.getId(),user.getUserId(),request.getBookmark());
        }
        else {
            throw new BodyInvalidGroupException("type 값이 잘못되었습니다. ('building' 또는 'review'여야 합니다.)");
        }
        return new ResTemplate<>(HttpStatus.OK,"처리 완료");
    }
}
