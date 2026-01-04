package JJinBBang.app.domain.building.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import JJinBBang.app.domain.building.dto.AgencySearchRequest;
import JJinBBang.app.domain.building.dto.AgencySearchResponse;
import JJinBBang.app.domain.building.service.AgencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/agency")
@RequiredArgsConstructor
public class AgencyController {

	private final AgencyService agencyService;

	/**
	 * 공인중개사 상호 검색
	 * GET /api/v1/building/agency/search?agencyName={agencyName}&num={num}&page={page}
	 * @param request AgencySearchRequest
	 * @return ResponseEntity<AgencySearchResponse>
	 */
	@GetMapping("/search")
	public ResponseEntity<AgencySearchResponse> searchAgencyList(
		@Valid AgencySearchRequest request
	) {
		 AgencySearchResponse response = agencyService.getAgencyList(request);
		return ResponseEntity.ok(response);
	}
}
