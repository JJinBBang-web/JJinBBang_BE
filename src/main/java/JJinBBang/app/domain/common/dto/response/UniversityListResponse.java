package JJinBBang.app.domain.common.dto.response;

import JJinBBang.app.global.common.dto.UniversityInfo;

import java.util.List;

public record UniversityListResponse(
        List<UniversityInfo> universityList
) {
}
