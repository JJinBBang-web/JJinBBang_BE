package JJinBBang.app.domain.building.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import JJinBBang.app.domain.building.dto.VWorldWfsResponse;
import JJinBBang.app.domain.building.entity.PlaceBuildingMap;
import JJinBBang.app.domain.building.infra.VWorldApiClient;
import JJinBBang.app.domain.building.repository.PlaceBuildingMapRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuildingCodeResolver {
	private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);
	private final VWorldApiClient vWorldApiClient;
	private static final ObjectMapper mapper = new ObjectMapper();
	private final PlaceBuildingMapRepository placeBuildingMapRepository;

	/**
	 * 카카오 장소 ID와 좌표(경도, 위도)를 받아서 건물 관리 번호를 반환합니다.
	 * 기존 매핑 정보가 있으면 그 정보를 우선 사용하고, 없으면 VWorld API를 호출하여 조회합니다.
	 * 수동 수정된 매핑 정보는 우선 사용하며, 수동 수정되지 않은 매핑 정보는 한달마다 재검증합니다.
	 * */
	@Transactional
	public String resolve(Double longitude, Double latitude, String kakaoPlaceId) {

		// 기존 매핑 정보 확인
		Optional<PlaceBuildingMap> opt = placeBuildingMapRepository.findById(kakaoPlaceId);

		// 기존 매핑 정보가 있다면, 수동 수정 여부 및 한달 경과 여부 확인
		if (opt.isPresent()) {
			PlaceBuildingMap map = opt.get();

			// 수동 수정했으면 기존 매핑 정보 사용
			if (map.isManualOverride()) {
				return map.getBuildingMgmtNo();
			}

			// 수동 수정 안 했으면 한달 경과 여부 확인
			if (map.needsMonthlyRecheck()) {

				// 한달 지났으면 API 재조회 후 매핑 정보 갱신
				String refreshed = fetchBuildingCodeFromApi(longitude, latitude);

				if (refreshed != null) {
					// 재조회 결과가 있다면, 매핑 정보 갱신
					map.updateBuildingMgmtNo(refreshed);
					placeBuildingMapRepository.save(map);
					return refreshed;
				} else {
					// 재조회 결과가 없다면, 기존 매핑 정보 유지
					log.warn("VWorld 재조회 결과가 없습니다. 기존 매핑 정보(kakaoPlaceId = {}, 건물관리번호 = {})를 유지하겠습니다",
						kakaoPlaceId, map.getBuildingMgmtNo());
					return map.getBuildingMgmtNo();
				}
			} else {
				// 한달 안 지났으면 기존 매핑 정보 사용
				return map.getBuildingMgmtNo();
			}
		} else {
			// 기존 매핑 정보가 없으면 API 조회 후 매핑 정보 저장
			String resolved = fetchBuildingCodeFromApi(longitude, latitude);
			if (resolved == null) {
				// API 조회 결과가 없으면 임시로 카카오 장소 ID 값 저장
				resolved = "KAKAO_PLACE_ID_" + kakaoPlaceId;
				log.warn("VWorld API: lon={}, lat={}에 건물 정보가 없습니다.", longitude, latitude);
			}

			// 새 매핑 정보 저장
			PlaceBuildingMap newMap = PlaceBuildingMap.of(kakaoPlaceId, latitude, longitude, resolved, false);
			placeBuildingMapRepository.save(newMap);

			return resolved;
		}
	}

	/**
	 * VWorld API를 호출하여 건물 관리 번호를 조회합니다.
	 * */
	private String fetchBuildingCodeFromApi(Double longitude, Double latitude) {
		VWorldWfsResponse.Response response = vWorldApiClient.searchBuildingByPoint(longitude, latitude).response();

		// API 호출 결과가 없으면, null 반환
		if ("NOT_FOUND".equals(response.status())) {
			return null;
		}
		if (response.result() == null
			|| response.result().featureCollection() == null
			|| response.result().featureCollection().features() == null
			|| response.result().featureCollection().features().isEmpty()) {
			return null;
		}

		// 가장 가까운 피처 선택
		List<VWorldWfsResponse.Feature> features = response.result().featureCollection().features();
		VWorldWfsResponse.Feature best = getClosestFeature(longitude, latitude, features);

		return best != null && best.properties() != null ? best.properties().bd_mgt_sn() : null;
	}

	/**
	 * 주어진 좌표(경도, 위도)에서 가장 가까운 피처를 선택합니다.
	 * 피처가 좌표를 포함하면 즉시 반환하고, 포함하는 피처가 없으면 가장 가까운 피처를 반환합니다.
	 * */
	private VWorldWfsResponse.Feature getClosestFeature(
		Double longitude, Double latitude,
		List<VWorldWfsResponse.Feature> features) {

		// 피처 목록이 없으면 null 반환
		if (features == null || features.isEmpty()) {
			return null;
		}

		// 좌표를 Point 객체로 생성
		Point point = GF.createPoint(new Coordinate(longitude, latitude));

		double minDistance = Double.MAX_VALUE;
		VWorldWfsResponse.Feature closestFeature = null;

		// 각 피처의 지오메트리를 확인하여 포함 여부 및 거리 계산
		for (VWorldWfsResponse.Feature feature : features) {
			VWorldWfsResponse.GeoJsonGeometry geo = feature.geometry();
			if (geo == null || geo.type() == null || geo.coordinates() == null) continue;

			// GeoJSON 문자열을 Geometry 객체로 변환
			Geometry g;
			try {
				String geoJson = mapper.writeValueAsString(
					Map.of("type", geo.type(), "coordinates", geo.coordinates())
				);
				GeoJsonReader geoJsonReader = new GeoJsonReader();
				g = geoJsonReader.read(geoJson);
			} catch (JsonProcessingException | ParseException e) {
				continue;
			}
			// 지오메트리가 없으면 다음 피처로
			if (g == null || g.isEmpty()) {
				continue;
			}

			// 좌표가 지오메트리 안에 있으면 즉시 반환
			if (g.covers(point)) {
				return feature;
			}

			// 좌표가 지오메트리 안에 없으면 거리 계산
			double distance = g.distance(point);

			// 가장 가까운 피처 갱신
			if (distance < minDistance) {
				minDistance = distance;
				closestFeature = feature;
			}

		}
		return closestFeature;
	}
}
