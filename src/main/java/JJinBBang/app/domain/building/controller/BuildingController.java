package JJinBBang.app.domain.building.controller;

import JJinBBang.app.domain.building.service.AgencyService;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import JJinBBang.app.domain.building.dto.BuildingDetailResponse;
import JJinBBang.app.domain.building.service.BuildingService;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/building")
@RequiredArgsConstructor
public class BuildingController {
	private final BuildingService buildingService;
	private final AgencyService agencyService;

	@GetMapping("/{buildingId}")
	public ResTemplate<BuildingDetailResponse> getBuildingDetail(
			@PathVariable Long buildingId,
			@NonNull @RequestParam Boolean isAgency,
			@AuthenticationPrincipal Users user
			) {

		BuildingDetailResponse data;
		if (isAgency) {
			data = agencyService.getAgencyDetail(buildingId, user);
        } else {
			data = buildingService.getBuildingDetail(buildingId, user);
		}
		return new ResTemplate<>(HttpStatus.OK, "건물 상세 불러오기 성공", data);
	}
}
