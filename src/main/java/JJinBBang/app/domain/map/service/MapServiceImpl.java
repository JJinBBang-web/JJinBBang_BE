package JJinBBang.app.domain.map.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import JJinBBang.app.domain.building.enums.ContractType;
import JJinBBang.app.domain.building.repository.BuildingLikesRepository;
import JJinBBang.app.domain.building.repository.BuildingsRepository;
import JJinBBang.app.domain.building.repository.ReviewLikesRepository;
import JJinBBang.app.domain.building.repository.ReviewsRepository;
import JJinBBang.app.domain.building.service.SearchInfo;
import JJinBBang.app.domain.common.dto.PaginatedResponse;
import JJinBBang.app.domain.common.enums.SortType;
import JJinBBang.app.domain.map.exception.MapNotFoundException;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.common.dto.InfoDto;
import org.springframework.stereotype.Service;

import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.common.dto.item.Filters;
import JJinBBang.app.domain.map.dto.item.Bounds;
import JJinBBang.app.domain.map.dto.request.MapMarkerRequest;
import JJinBBang.app.domain.map.dto.request.SearchMarkerRequest;
import JJinBBang.app.domain.map.dto.request.NearByMapItemRequest;
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
	private final ReviewsRepository reviewsRepository;
	private final BuildingLikesRepository buildingLikesRepository;
	private final ReviewLikesRepository reviewLikesRepository;
	private final SearchInfo searchInfo;

	@Override
	public List<MarkerInfo> getMapMarkers(MapMarkerRequest request) {
		Bounds bounds = request.bounds();
		Filters filters = request.filters();

		// 유효성 검사
		validateBounds(bounds);
		validateFilterRanges(filters);
		validateKeywordLimit(filters.reviewKeyword());

		ContractType contractType = parseContractType(filters.contractType());

		// 모든 조건 기반으로 건물 리스트를 먼저 조회
		List<Buildings> buildings = buildingsRepository.findMarkersWithinBounds(
			bounds.neLat(), bounds.neLng(),
			bounds.swLat(), bounds.swLng(),
			filters.buildType(), contractType,
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
	public PaginatedResponse<InfoDto> searchMarker(SearchMarkerRequest request, Users user) {
		Filters filters = request.filters();

		// 기본 유효성 검사
		if (request.keyword() == null || request.keyword().trim().length() <= 1) {
			throw MapInvalidException.invalidKeyword();
		}
		validateFilterRanges(filters);
		validateKeywordLimit(filters.reviewKeyword());

		ContractType contractType = parseContractType(filters.contractType());

		List<Buildings> buildings = buildingsRepository.searchBuildings(
			request.keyword(),
			filters.buildType(), contractType,
			filters.depositMin(), filters.depositMax(),
			filters.monthlyRentMin(), filters.monthlyRentMax(),
			filters.inMaintenanceCost(),
			filters.reviewKeyword(),
			filters.campus()
		);

		if (buildings.isEmpty()) {
			throw MapNoContentException.searchFailed();
		}

		List<InfoDto> items = new ArrayList<>();
		for (Buildings b : buildings) {
			if (filters.viewType() == ViewType.REVIEW) {
				b.getReviews().forEach(r -> {
					boolean liked = user != null && reviewLikesRepository
						.findByReviewIdAndUserUserId(r.getId(), user.getUserId())
						.isPresent();
					try {
						items.add(searchInfo.reviewSearch(r.getId(), liked));
					} catch (Exception e) {
						throw MapNotFoundException.notFoundReview();
					}
				});
			} else {
				boolean liked = user != null && buildingLikesRepository
					.findByBuildingAndUser(b, user)
					.isPresent();
				items.add(searchInfo.buildingSearch(b.getId(), liked));
			}
		}

		int from = Math.max(0, (request.page() - 1) * request.num());
		int to = Math.min(items.size(), from + request.num());
		if (from > to) {
			from = to;
		}

		List<InfoDto> paged = items.subList(from, to);

		return PaginatedResponse.<InfoDto>builder()
			.num(paged.size())
			.page(request.page())
			.itemNum((long) items.size())
			.items(paged)
			.build();
	}

	@Override
	public PaginatedResponse<InfoDto> nearByMapItems(NearByMapItemRequest request, Users user) {
		List<Long> ids = request.idList();

		// id가 존재하지 않는 경우 예외처리
		if (ids == null || ids.isEmpty()) {
			throw MapNoContentException.emptyIdList();
		}

		List<InfoDto> items = new ArrayList<>();
		// View 타입에 따른 리뷰, 건물별 조회
		for (Long id : ids) {
			if (request.type() == ViewType.REVIEW) {
				boolean liked = user != null && reviewLikesRepository
						.findByReviewIdAndUserUserId(id, user.getUserId())
						.isPresent();
				try {
					items.add(searchInfo.reviewSearch(id, liked));
				} catch (Exception e) {
					throw MapNotFoundException.notFoundReview();
				}
			} else {
				Buildings building = buildingsRepository.findById(id)
						.orElseThrow(MapNotFoundException::notFoundBuilding);
				boolean liked = user != null && buildingLikesRepository
						.findByBuildingAndUser(building, user)
						.isPresent();
				items.add(searchInfo.buildingSearch(id, liked));
			}
		}

		// 정렬
		Comparator<InfoDto> comparator = getComparator(request.sortBy(), request.type());
		if (comparator != null) {
			items.sort(comparator);
		}

		// 페이징 처리 (page는 1부터 시작)
		int from = Math.max(0, (request.page() - 1) * request.num());
		int to = Math.min(items.size(), from + request.num());
		if (from > to) {
			from = to;
		}
		List<InfoDto> paged = items.subList(from, to);

		return PaginatedResponse.<InfoDto>builder()
				.num(paged.size())
				.page(request.page())
				.itemNum((long) items.size())
				.items(paged)
				.build();
	}

	private Comparator<InfoDto> getComparator(SortType sort, ViewType type) {
		return switch (sort) {
			case LIKES -> Comparator.comparing(this::extractLikeCount).reversed();
			case STARS -> Comparator.comparing(this::extractRating, Comparator.nullsLast(BigDecimal::compareTo)).reversed();
			case LATEST -> Comparator.comparing(this::extractUpdatedAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed();
			case RCMND -> {
				if (type == ViewType.BUILDING) {
					yield Comparator.comparing(this::extractReviewCount).reversed();
				} else {
					yield null; // 리뷰 추천순은 요청 순서 유지
				}
			}
		};
	}

	private Integer extractReviewCount(InfoDto dto) {
		if (dto.generalBuildingInfo() != null) {
			return dto.generalBuildingInfo().reviewCount();
		}
		if (dto.agencyBuildingInfo() != null) {
			return dto.agencyBuildingInfo().reviewCount();
		}
		return 0;
	}

	private Integer extractLikeCount(InfoDto dto) {
		return dto.reviewInfo() != null ? dto.reviewInfo().likeCount() : 0;
	}

	private BigDecimal extractRating(InfoDto dto) {
		if (dto.generalReviewInfo() != null) {
			return dto.generalReviewInfo().rating();
		}
		if (dto.dormitoryReviewInfo() != null) {
			return dto.dormitoryReviewInfo().rating();
		}
		if (dto.agencyReviewInfo() != null) {
			return dto.agencyReviewInfo().rating();
		}
		if (dto.generalBuildingInfo() != null) {
			return dto.generalBuildingInfo().rating();
		}
		if (dto.dormitoryBuildingInfo() != null) {
			return dto.dormitoryBuildingInfo().rating();
		}
		if (dto.agencyBuildingInfo() != null) {
			return dto.agencyBuildingInfo().rating();
		}
		return BigDecimal.ZERO;
	}

	private LocalDateTime extractUpdatedAt(InfoDto dto) {
		return dto.reviewInfo() != null ? dto.reviewInfo().updateAt() : null;
	}



	// Validation 메서드
	// 위도/경도 범위, 필터링 조건, 키워드 개수 등 유효성 검사

	// 위도/경도 범위 검사
	private void validateBounds(Bounds bounds) {
		if (bounds.neLat() < bounds.swLat() || bounds.neLng() < bounds.swLng()) {
			throw MapUnprocessableException.invalidGeographicBounds(); // 위도/경도 범위 역전
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

	private ContractType parseContractType(String type) {
		if (type == null) {
			return null;
		}
		return switch (type) {
			case "MONTHLY_RENT" -> ContractType.MONTHLY_RENT;
			case "DEPOSIT_RENT" -> ContractType.DEPOSIT_RENT;
			case "ALL" -> null;
			default -> null;
		};
	}
}
