package JJinBBang.app.domain.common.controller;

import JJinBBang.app.domain.common.dto.CampusSearchResponse;
import JJinBBang.app.domain.common.dto.UniversityResponseDto;
import JJinBBang.app.domain.common.dto.response.UniversityListResponse;
import JJinBBang.app.domain.common.service.UniversityService;
import JJinBBang.app.global.common.dto.UniversityInfo;
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

    private final UniversityService universityService;

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

    @GetMapping("/location")
    public ResTemplate<UniversityListResponse> getUniversityListByLocation(
            @RequestParam(required = false) Double lat, // 위도
            @RequestParam(required = false) Double lng // 경도
    ) {
        List<UniversityInfo> universityInfos;
        if (lat != null && lng != null) {
            universityInfos = universityService.getUniversityListByLocation(lat, lng);
        } else {
            universityInfos = universityService.getUniversityListBasic();
        }
        UniversityListResponse res = new UniversityListResponse(universityInfos);
        return new ResTemplate<>(HttpStatus.OK, "대학교 리스트 조회 성공", res);
      
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
