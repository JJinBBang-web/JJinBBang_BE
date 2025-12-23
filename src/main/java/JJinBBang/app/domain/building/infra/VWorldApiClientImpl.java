package JJinBBang.app.domain.building.infra;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import JJinBBang.app.domain.building.dto.VWorldWfsGetFeatureRequest;
import JJinBBang.app.domain.building.dto.VWorldFeatureCollection;
import JJinBBang.app.domain.building.dto.VWorld2DDataRequest;
import JJinBBang.app.domain.building.dto.VWorld2DDataResponse;

@Component
public class VWorldApiClientImpl implements VWorldApiClient {

	private final RestTemplate restTemplate;

	@Value("${vworld.base-url}")
	private String baseUrl;
	@Value("${vworld.api-key}")
	private String apiKey;
	@Value("${vworld.buffer-meter:5}")
	private Integer bufferMeter;
	@Value("${vworld.domain}")
	private String domain;

	public VWorldApiClientImpl(RestTemplateBuilder builder) {
		this.restTemplate = builder.build();
	}

	@Override
	public VWorld2DDataResponse searchBuildingByPoint(Double longitude, Double latitude) {
		VWorld2DDataRequest request = VWorld2DDataRequest.byPoint(
			apiKey,
			longitude,
			latitude,
			bufferMeter
		);

		return search(request);
	}

	@Override
	public VWorldFeatureCollection searchAgencies(VWorldWfsGetFeatureRequest req) {

		URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/req/wfs")
			.queryParams(req.toQueryParams(apiKey, domain))
			.build()
			.encode(StandardCharsets.UTF_8)  // filter XML + 한글 인코딩
			.toUri();

		return restTemplate.getForObject(uri, VWorldFeatureCollection.class);
	}

	private VWorld2DDataResponse search(VWorld2DDataRequest request) {
		RestTemplate restTemplate = new RestTemplate();

		URI uri = UriComponentsBuilder.fromUriString(this.baseUrl+"/req/data")
			.queryParams(request.toQueryParams())
			.build()
			.toUri();

		return restTemplate.getForObject(uri, VWorld2DDataResponse.class);
	}
}
