package JJinBBang.app.domain.building.controller;

import JJinBBang.app.domain.building.dto.GetUserBookmarkRequest;
import JJinBBang.app.global.common.dto.InfoDto;
import JJinBBang.app.domain.building.dto.SetUserBookmarkRequest;
import JJinBBang.app.domain.building.exception.UserBookmarkInvalidGroupException;
import JJinBBang.app.domain.building.service.BookmarkService;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/user/bookmark")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;
    @GetMapping("")
    public ResTemplate<List<InfoDto>> getBookmarks(@AuthenticationPrincipal Users user, @PageableDefault(size = 10, page = 0)Pageable pageable , @Valid @ModelAttribute GetUserBookmarkRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining(" / "));

            throw new UserBookmarkInvalidGroupException(errorMessage);
        }

        return new ResTemplate<>(HttpStatus.OK, "처리 완료",bookmarkService.searchBookmark(user.getUserId(),pageable,request));
    }

    @PostMapping("")
    public ResTemplate<String> postBookmarks(@AuthenticationPrincipal Users user,
                                             @Valid @RequestBody SetUserBookmarkRequest request,
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining(" / "));

            throw new UserBookmarkInvalidGroupException(errorMessage);
        }
        switch (request.type()) {
            case "building" -> bookmarkService.buildingBookmark(request.id(), user, request.bookmark());
            case "review" -> bookmarkService.reviewBookmark(request.id(), user, request.bookmark());
            case "agency" -> bookmarkService.agencyBookmark(request.id(), user, request.bookmark());
        }
        return new ResTemplate<>(HttpStatus.OK,"처리 완료");
    }
}
