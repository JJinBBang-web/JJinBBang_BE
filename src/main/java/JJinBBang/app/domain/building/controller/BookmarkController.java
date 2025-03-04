package JJinBBang.app.domain.building.controller;

import JJinBBang.app.domain.building.dto.SetUserBookmarkRequest;
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
    public ResTemplate postBookmarks(@AuthenticationPrincipal Users user, @RequestBody SetUserBookmarkRequest request) {
        if (request.getType().equals("building")){
            bookmarkService.BuildingBookmark(request.getTypeId(),user.getUserId(),request.isLiked());
        }
        else if (request.getType().equals("review")){
            bookmarkService.ReviewBookmark(request.getTypeId(),user.getUserId(),request.isLiked());
        }
        return new ResTemplate(HttpStatus.OK,"qwe");
    }
}
