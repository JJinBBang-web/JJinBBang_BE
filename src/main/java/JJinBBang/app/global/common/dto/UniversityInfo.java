package JJinBBang.app.global.common.dto;

public record UniversityInfo(
        Long id,
        String universityName,
        String logoImageUrl,
        CampusInfo campusInfo
) {
}
