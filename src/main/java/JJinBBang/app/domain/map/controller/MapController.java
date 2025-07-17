package JJinBBang.app.domain.map.controller;

import java.util.List;

import JJinBBang.app.domain.common.dto.PaginatedResponse;
import JJinBBang.app.domain.map.dto.request.NearByMapItemRequest;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.common.dto.InfoDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import JJinBBang.app.domain.map.dto.request.MapMarkerRequest;
import JJinBBang.app.domain.map.service.MapService;
import JJinBBang.app.global.common.dto.MarkerInfo;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/map")
@RequiredArgsConstructor
public class MapController {
	private final MapService mapService;

	@PostMapping("/markers")
	public ResTemplate<List<MarkerInfo>> getMapMarkers(@RequestBody MapMarkerRequest request) {
		log.info("지도 마커 조회 성공");
		List<MarkerInfo> response = mapService.getMapMarkers(request);
		return new ResTemplate<>(HttpStatus.OK, "지도 마커 조회 성공", response);
	}

	@PostMapping("/markers/nearby")
	public ResTemplate<PaginatedResponse<InfoDto>> getNearByMarkers(
			@AuthenticationPrincipal Users user,
			@Valid @RequestBody NearByMapItemRequest request
	) {
		PaginatedResponse<InfoDto> response = mapService.nearByMapItems(request, user);
		return new ResTemplate<>(HttpStatus.OK, "내 주변 찐빵 조회 성공", response);
	}
}
