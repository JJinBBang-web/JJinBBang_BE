package JJinBBang.app.domain.user.controller;

import JJinBBang.app.domain.user.dto.ApiResponse;
import JJinBBang.app.domain.user.dto.UniversityResponseDto;
import JJinBBang.app.domain.user.exception.UniversityNotFoundException;
import JJinBBang.app.domain.user.service.UniversityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/univ")
@RequiredArgsConstructor
public class UniversityController {

    private final UniversityService universityService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UniversityResponseDto>>> getUniversityList(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "15") int limit) {

        List<UniversityResponseDto> universityList = universityService.getUniversityList(offset, limit);

        if (universityList.isEmpty()) {
            throw new UniversityNotFoundException();
        }

        return ResponseEntity.ok(new ApiResponse<>(200, "조회 성공", universityList));
    }
}
