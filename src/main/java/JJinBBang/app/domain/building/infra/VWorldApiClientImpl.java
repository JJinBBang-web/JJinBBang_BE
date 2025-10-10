package JJinBBang.app.domain.building.infra;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import JJinBBang.app.domain.building.dto.VWorldRequest;
import JJinBBang.app.domain.building.dto.VWorldResponse;

@Component
public class VWorldApiClientImpl implements VWorldApiClient {

	@Value("${vworld.base-url}")
	private String baseUrl;
	@Value("${vworld.api-key}")
	private String apiKey;
	@Value("${vworld.buffer-meter:5}")
	private Integer bufferMeter;

	@Override
	public VWorldResponse searchByPoint(Double longitude, Double latitude) {
		VWorldRequest request = VWorldRequest.byPoint(
			apiKey,
			longitude,
			latitude,
			bufferMeter
		);

		return search(request);
	}

	private VWorldResponse search(VWorldRequest request) {
		RestTemplate restTemplate = new RestTemplate();

		URI uri = UriComponentsBuilder.fromUriString(this.baseUrl)
			.queryParams(request.toQueryParams())
			.build()
			.toUri();

		return restTemplate.getForObject(uri, VWorldResponse.class);
	}
}
