package JJinBBang.app.domain.map.service;

import java.util.List;

import JJinBBang.app.domain.common.dto.PaginatedResponse;
import JJinBBang.app.domain.map.dto.request.MapMarkerRequest;
import JJinBBang.app.domain.map.dto.request.SearchMarkerRequest;
import JJinBBang.app.domain.map.dto.request.NearByMapItemRequest;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.common.dto.InfoDto;
import JJinBBang.app.global.common.dto.MarkerInfo;
import JJinBBang.app.global.common.dto.SearchInfoDto;

public interface MapService {
	/**
	 * 지도 마커를 조회하는 서비스
	 *
	 * @param request  위도/경도 범위(네모박스), 필터링 조건
	 * @return 마커 목록
	 */
	List<MarkerInfo> getMapMarkers(MapMarkerRequest request);

	/**
	 * 키워드와 조건들을 통해 검색을 진행하는 서비스
	 *
	 * @param request keyword, 페이지네이션, 필터
	 * @return 마커 상세 정보
	 */
	PaginatedResponse<SearchInfoDto> searchMarker(SearchMarkerRequest request, Users user);

	/**
	 * 주변 건물, 리뷰들을 상세 조회하는 서비스
	 *
	 * @param request 페이지네이션, 보기유형, 정렬, idList
	 * @param user 유저 id 및 정보
	 * @return 마커 목록
	 */
	PaginatedResponse<InfoDto> nearByMapItems(NearByMapItemRequest request, Users user);
}
