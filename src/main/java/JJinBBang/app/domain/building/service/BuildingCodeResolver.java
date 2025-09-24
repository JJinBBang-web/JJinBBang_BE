package JJinBBang.app.domain.building.service;

import java.util.List;
import java.util.Map;

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

import JJinBBang.app.domain.building.dto.VWorldResponse;
import JJinBBang.app.domain.building.infra.VWorldApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuildingCodeResolver {
	private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);
	private final VWorldApiClient vWorldApiClient;
	private static final ObjectMapper mapper = new ObjectMapper(); // 추가

	public String resolve(Double longitude, Double latitude, String buildingCode) {

		// VWorld API를 사용하여 좌표 주위 5m 건물 목록 조회
		VWorldResponse.Response res = vWorldApiClient.searchByPoint(longitude, latitude).response();

		// 건물이 없으면 임시로 buildingCode(카카오 장소 ID) 반환
		if (res.status().equals("NOT_FOUND")) {
			log.warn(
				"VWorld API: 해당 좌표에 건물이 없습니다. longitude={}, latitude={}, 임시 건물 번호({})를 저장합니다.",
				longitude, latitude, "KAKAO_PLACE_ID_"+buildingCode
			);
			return "KAKAO_PLACE_ID_"+buildingCode;
		}

		// 조회된 건물 목록
		List<VWorldResponse.Feature> features = res.result().featureCollection().features();

		// 좌표 기준으로 건물 목록 중에서 가장 가까운 건물의 bd_mgt_sn(건물관리번호) 추출
		VWorldResponse.Feature closestFeature = getClosestFeature(longitude, latitude, features);

		// 좌표와 가장 가까운 건물의 건물관리번호 반환
		return closestFeature.properties().bd_mgt_sn();
	}

	private VWorldResponse.Feature getClosestFeature(
		Double longitude, Double latitude,
		List<VWorldResponse.Feature> features) {

		if (features == null || features.isEmpty()) {
			return null;
		}

		Point point = GF.createPoint(new Coordinate(longitude, latitude));

		double minDistance = Double.MAX_VALUE;
		VWorldResponse.Feature closestFeature = null;

		for (VWorldResponse.Feature feature : features) {
			VWorldResponse.GeoJsonGeometry geo = feature.geometry();
			if (geo == null || geo.type() == null || geo.coordinates() == null) continue;

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
			if (g == null || g.isEmpty()) {
				continue;
			}

			if (g.covers(point)) {
				return feature;
			}

			double distance = g.distance(point);
			if (distance < minDistance) {
				minDistance = distance;
				closestFeature = feature;
			}

		}

		return closestFeature;
	}
}
