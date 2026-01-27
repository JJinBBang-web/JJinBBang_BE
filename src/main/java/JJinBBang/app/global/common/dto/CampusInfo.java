package JJinBBang.app.global.common.dto;

public record CampusInfo(
        Long id,
        String campusName,
        String logoImageUrl,
        String campusAddress,
        Double latitude,
        Double longitude
) {
}
