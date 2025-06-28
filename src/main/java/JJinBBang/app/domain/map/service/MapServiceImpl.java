package JJinBBang.app.domain.map.service;

import java.util.List;

import JJinBBang.app.domain.building.repository.BuildingsRepository;
import org.springframework.stereotype.Service;

import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.common.dto.item.Filters;
import JJinBBang.app.domain.map.dto.item.Bounds;
import JJinBBang.app.domain.map.dto.request.MapMarkerRequest;
import JJinBBang.app.domain.map.dto.request.SearchMarkerRequest;
import JJinBBang.app.domain.map.dto.request.NearByMapItemRequest;
import JJinBBang.app.domain.map.dto.response.MapItemDetailResponse;
import JJinBBang.app.domain.map.exception.MapInvalidException;
import JJinBBang.app.domain.map.exception.MapNoContentException;
import JJinBBang.app.domain.map.exception.MapUnprocessableException;
import JJinBBang.app.global.common.dto.MarkerInfo;
import JJinBBang.app.global.common.enums.KeywordType;
import JJinBBang.app.global.common.enums.ViewType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapServiceImpl implements MapService{
	private final BuildingsRepository buildingsRepository;


	@Override
	public List<MarkerInfo> getMapMarkers(MapMarkerRequest request) {
		Bounds bounds = request.bounds();
		Filters filters = request.filters();

		// 유효성 검사
		validateBounds(bounds);
		validateFilterRanges(filters);
		validateKeywordLimit(filters.reviewKeyword());

		// 모든 조건 기반으로 건물 리스트를 먼저 조회
		List<Buildings> buildings = buildingsRepository.findMarkersWithinBounds(
			bounds.neLat(), bounds.neLng(),
			bounds.swLat(), bounds.swLng(),
			filters.buildType(), filters.contractType(),
			filters.depositMin(), filters.depositMax(),
			filters.monthlyRentMin(), filters.monthlyRentMax(),
			filters.inMaintenanceCost(),
			filters.reviewKeyword(),
			filters.campus()
		);

		if (buildings.isEmpty()) {
			if (filters.viewType() == ViewType.REVIEW) {
				throw MapNoContentException.notFoundReview();
			} else {
				throw MapNoContentException.notFoundBuilding();
			}
		}

		if (filters.viewType() == ViewType.REVIEW) {
			// 건물에 딸린 리뷰 ID별로 마커 생성
			return buildings.stream()
				.flatMap(b -> b.getReviews().stream()
					.map(r -> new MarkerInfo(r.getId(), b.getBuildingLat(), b.getBuildingLot())))
				.toList();
		} else {
			// 건물별 마커
			return buildings.stream()
				.map(b -> new MarkerInfo(b.getId(), b.getBuildingLat(), b.getBuildingLot()))
				.toList();
		}
	}

	@Override
	public MapItemDetailResponse searchMarker(SearchMarkerRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MapItemDetailResponse nearByMapItems(NearByMapItemRequest request) {
		// TODO Auto-generated method stub
		return null;
	}


	// Validation 메서드
	// 위도/경도 범위, 필터링 조건, 키워드 개수 등 유효성 검사

	// 위도/경도 범위 검사
	private void validateBounds(Bounds bounds) {
		if (bounds.neLat() < bounds.swLat() || bounds.neLng() < bounds.swLng()) {
			throw MapUnprocessableException.invalidDepositRange(); // 위도/경도 범위 역전
		}
	}

	// 필터링 조건 검사
	private void validateFilterRanges(Filters filters) {
		if (filters.depositMax() != null && filters.depositMin() > filters.depositMax()) {
			throw MapUnprocessableException.invalidDepositRange();
		}
		if (filters.monthlyRentMax() != null && filters.monthlyRentMin() > filters.monthlyRentMax()) {
			throw MapUnprocessableException.invalidMonthlyRentRange();
		}
	}

	// 키워드 개수 검사
	private void validateKeywordLimit(List<KeywordType> keywords) {
		if (keywords != null && keywords.size() > 5) {
			throw MapInvalidException.invalidKeyword();
		}
	}
}
