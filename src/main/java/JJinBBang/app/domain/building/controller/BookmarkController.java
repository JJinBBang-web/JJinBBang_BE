package JJinBBang.app.domain.building.controller;

import JJinBBang.app.domain.building.dto.SetUserBookmarkRequest;
import JJinBBang.app.domain.building.exception.setUserBookmarkInvalidGroupException;
import JJinBBang.app.domain.building.service.BookmarkService;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/user/bookmark")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("")
    public ResTemplate<String> postBookmarks(@AuthenticationPrincipal Users user,
                                             @Valid @RequestBody SetUserBookmarkRequest request,
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining(", "));

            throw new setUserBookmarkInvalidGroupException(errorMessage);
        }
        if (request.getType().equals("building")){

            bookmarkService.BuildingBookmark(request.getId(),user.getUserId(),request.getBookmark());
        }
        else if (request.getType().equals("review")){
            // 위와 동일
            bookmarkService.ReviewBookmark(request.getId(),user.getUserId(),request.getBookmark());
        }
        return new ResTemplate<>(HttpStatus.OK,"처리 완료");
    }
}
