package JJinBBang.app.domain.building.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import JJinBBang.app.domain.building.dto.BuildingDetailResponse;
import JJinBBang.app.domain.building.service.BuildingService;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/building")
@RequiredArgsConstructor
public class BuildingController {
	private final BuildingService buildingService;

	@GetMapping("/{buildingId}")
	public ResTemplate<BuildingDetailResponse> getBuildingDetail(@PathVariable Long buildingId) {
		//TODO 토큰에서 사용자 정보 얻는 로직 개발 전까지 하드코딩 사용
		Long userId = 1L;

		BuildingDetailResponse data = buildingService.getBuildingDetail(buildingId, userId);
		return new ResTemplate<>(HttpStatus.OK, "건물 상세 불러오기 성공", data);
	}
}
