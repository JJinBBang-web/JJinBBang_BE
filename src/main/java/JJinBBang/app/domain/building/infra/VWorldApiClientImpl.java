package JJinBBang.app.domain.building.infra;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import JJinBBang.app.domain.building.dto.VWorldAddressToCoordRequest;
import JJinBBang.app.domain.building.dto.VWorldAddressToCoordResponse;
import JJinBBang.app.domain.building.dto.VWorldEBOfficeRequest;
import JJinBBang.app.domain.building.dto.VWorldEBOfficeResponse;
import JJinBBang.app.domain.building.dto.VWorldWfsRequest;
import JJinBBang.app.domain.building.dto.VWorldWfsResponse;

@Component
public class VWorldApiClientImpl implements VWorldApiClient {

	@Value("${vworld.base-url}")
	private String baseUrl;
	@Value("${vworld.api-key}")
	private String apiKey;
	@Value("${vworld.buffer-meter:5}")
	private Integer bufferMeter;
	@Value("${vworld.domain}")
	private String domain;

	@Override
	public VWorldWfsResponse searchBuildingByPoint(Double longitude, Double latitude) {
		VWorldWfsRequest request = VWorldWfsRequest.byPoint(
			apiKey,
			longitude,
			latitude,
			bufferMeter
		);

		return searchWfs(request);
	}

	@Override
	public VWorldEBOfficeResponse searchAgencies(VWorldEBOfficeRequest vWorldRequest) {
		RestTemplate restTemplate = new RestTemplate();

		URI uri = UriComponentsBuilder.fromUriString(this.baseUrl+"/ned/data/getEBOfficeInfo")
			.queryParams(vWorldRequest.toQueryParams(apiKey, domain))
			.build()
			.encode(StandardCharsets.UTF_8)          // ✅ 한글 쿼리 파라미터 인코딩
			.toUri();

		return restTemplate.getForObject(uri, VWorldEBOfficeResponse.class);
	}

	@Override
	public VWorldAddressToCoordResponse geocode(VWorldAddressToCoordRequest request) {
		RestTemplate restTemplate = new RestTemplate();

		URI uri = UriComponentsBuilder.fromUriString(this.baseUrl+"/req/address")
			.queryParams(request.toQueryParams(apiKey))
			.build()
			.encode(StandardCharsets.UTF_8)          // ✅ 한글 쿼리 파라미터 인코딩
			.toUri();

		return restTemplate.getForObject(uri, VWorldAddressToCoordResponse.class);
	}

	private VWorldWfsResponse searchWfs(VWorldWfsRequest request) {
		RestTemplate restTemplate = new RestTemplate();

		URI uri = UriComponentsBuilder.fromUriString(this.baseUrl+"/req/data")
			.queryParams(request.toQueryParams())
			.build()
			.toUri();

		return restTemplate.getForObject(uri, VWorldWfsResponse.class);
	}
}
