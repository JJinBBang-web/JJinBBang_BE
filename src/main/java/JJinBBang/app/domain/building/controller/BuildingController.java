package JJinBBang.app.domain.building.controller;

import JJinBBang.app.domain.building.dto.DormitoryListResponse;
import JJinBBang.app.domain.building.service.AgencyService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import JJinBBang.app.domain.building.dto.BuildingDetailResponse;
import JJinBBang.app.domain.building.dto.PageRequest;
import JJinBBang.app.domain.building.dto.ReviewSummaryResponse;
import JJinBBang.app.domain.building.service.BuildingService;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.building.service.ReviewService;
import JJinBBang.app.domain.common.dto.PaginatedResponse;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/building")
@RequiredArgsConstructor
public class BuildingController {
	private final BuildingService buildingService;
	private final AgencyService agencyService;
	private final ReviewService reviewService;

	@GetMapping("/{buildingId}")
	public ResTemplate<BuildingDetailResponse> getBuildingDetail(
			@PathVariable Long buildingId,
			@RequestParam(defaultValue = "false") Boolean isAgency,
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

	@GetMapping("/{buildingId}/review")
	public ResTemplate<PaginatedResponse<ReviewSummaryResponse>> getBuildingReview(
		@PathVariable Long buildingId,
		@AuthenticationPrincipal Users user,
		PageRequest pageRequest,
		@RequestParam(defaultValue = "false") Boolean isAgency
	) {

		PaginatedResponse<ReviewSummaryResponse> data = reviewService.getReviewList(buildingId, isAgency, user, pageRequest);
		return new ResTemplate<>(HttpStatus.OK, "조회 성공", data);
	}

	@GetMapping("/dormitory")
	public ResTemplate<DormitoryListResponse> getDormitoryList(@RequestParam Long universityId) {
		DormitoryListResponse data = buildingService.getDormitoryList(universityId);
		return new ResTemplate<>(HttpStatus.OK, "기숙사 목록 조회 성공", data);
	}
}
