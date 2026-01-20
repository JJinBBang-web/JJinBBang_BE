package JJinBBang.app.domain.common.controller;

import JJinBBang.app.domain.common.dto.CampusSearchResponse;
import JJinBBang.app.domain.common.dto.UniversityResponseDto;
import JJinBBang.app.domain.common.service.UniversityServiceImpl;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/univ")
@RequiredArgsConstructor
public class UniversityController {

    private final UniversityServiceImpl universityService;

    @GetMapping
    public ResTemplate<?> getUniversityList(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "15") int limit
    ) {

        List<UniversityResponseDto> response = universityService.getUniversityList(offset, limit);

        return new ResTemplate<>(
                HttpStatus.OK,
                "대학교 리스트 조회 성공",
                response
        );
    }

    @Validated
    @GetMapping("/search")
    public ResTemplate<CampusSearchResponse> searchCampuses(
        @RequestParam
        String query,

        @RequestParam(defaultValue = "15")
        int limit,

        @RequestParam(defaultValue = "0")
        int offset
    ) {
        CampusSearchResponse data = universityService.searchCampuses(query, limit, offset);

        return new ResTemplate<>(
            HttpStatus.OK,
            "대학교 검색 성공",
            data
        );
    }
}
