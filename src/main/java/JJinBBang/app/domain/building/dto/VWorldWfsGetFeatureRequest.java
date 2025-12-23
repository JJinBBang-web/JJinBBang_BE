package JJinBBang.app.domain.building.dto;

import java.util.Objects;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public record VWorldWfsGetFeatureRequest(
	int maxFeatures,
	String filterXml,
	String sortBy
) {
	public MultiValueMap<String, String> toQueryParams(String apiKey, String domain) {
		Objects.requireNonNull(apiKey, "apiKey must not be null");

		MultiValueMap<String, String> q = new LinkedMultiValueMap<>();
		q.add("key", apiKey);
		q.add("service", "WFS");
		q.add("request", "GetFeature");
		q.add("version", "1.1.0");
		q.add("typename", "dt_d170");
		q.add("output", "application/json");
		q.add("srsname", "EPSG:4326");

		q.add("maxfeatures", String.valueOf(maxFeatures));
		q.add("filter", filterXml);

		// 결과 순서 고정(커서 페이징의 핵심)
		if (sortBy != null && !sortBy.isBlank()) q.add("sortby", sortBy);

		if (domain != null && !domain.isBlank()) q.add("domain", domain);
		return q;
	}

	public static VWorldWfsGetFeatureRequest of(String agencyName, int num, String cursor) {
		int safeNum = Math.max(1, Math.min(num, 10));
		String filter = buildAgencyNameFilterWithCursor(agencyName, cursor);

		// 커서 페이징 = 정렬 기준 반드시 고정
		String sortBy = "brkpg_regist_no A"; // 문자열 정렬 기준(사전식)

		return new VWorldWfsGetFeatureRequest(safeNum, filter, sortBy);
	}

	private static String buildAgencyNameFilterWithCursor(String agencyNameRaw, String cursorRaw) {
		String keyword = escapeXml(agencyNameRaw == null ? "" : agencyNameRaw.trim());
		String cursor = (cursorRaw == null || cursorRaw.isBlank()) ? null : escapeXml(cursorRaw.trim());

		String likePart = """
            <ogc:PropertyIsLike wildCard="*" singleChar="_" escapeChar="\\">
              <ogc:PropertyName>bsnm_cmpnm</ogc:PropertyName>
              <ogc:Literal>*%s*</ogc:Literal>
            </ogc:PropertyIsLike>
            """.formatted(keyword);

		if (cursor == null) {
			return """
                <ogc:Filter xmlns:ogc="http://www.opengis.net/ogc">
                  %s
                </ogc:Filter>
                """.formatted(likePart);
		}

		// cursor 이후만 가져오기: brkpg_regist_no > cursor
		return """
            <ogc:Filter xmlns:ogc="http://www.opengis.net/ogc">
              <ogc:And>
                %s
                <ogc:PropertyIsGreaterThan>
                  <ogc:PropertyName>brkpg_regist_no</ogc:PropertyName>
                  <ogc:Literal>%s</ogc:Literal>
                </ogc:PropertyIsGreaterThan>
              </ogc:And>
            </ogc:Filter>
            """.formatted(likePart, cursor);
	}

	private static String escapeXml(String s) {
		return s.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("\"", "&quot;")
			.replace("'", "&apos;");
	}
}
