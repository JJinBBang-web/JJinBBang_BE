package JJinBBang.app.domain.user.controller;

import JJinBBang.app.domain.user.dto.UniversityResponseDto;
import JJinBBang.app.domain.user.exception.UniversityNotFoundException;
import JJinBBang.app.domain.user.service.UniversityService;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ResTemplate<List<UniversityResponseDto>>> getUniversityList(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "15") int limit) {

        ResTemplate<List<UniversityResponseDto>> response = universityService.getUniversityList(offset, limit);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
