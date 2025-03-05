package JJinBBang.app.domain.common.controller;

import JJinBBang.app.domain.common.dto.UnivCampusListResponse;
import JJinBBang.app.domain.common.entity.Campuses;
import JJinBBang.app.domain.common.service.CampusesService;
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
@RequestMapping("/api/v1/user/univ/campus")
@RequiredArgsConstructor
public class CampusesController {

    private final CampusesService campusesService;

    @GetMapping("")
    public ResTemplate<UnivCampusListResponse> getCampuses(@RequestParam(name = "universityName") String universityName) {
        List<Campuses> campuses = campusesService.findCampuses(universityName);

        UnivCampusListResponse responseDTO = UnivCampusListResponse.CampusDTO.from(campuses);

        return new ResTemplate<>(HttpStatus.OK, "조회 성공", responseDTO);
    }
}
