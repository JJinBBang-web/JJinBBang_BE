package JJinBBang.app.domain.common.service;

import JJinBBang.app.domain.common.dto.UniversityResponseDto;
import JJinBBang.app.global.common.dto.UniversityInfo;

import java.util.List;

public interface UniversityService {

    List<UniversityResponseDto> getUniversityList(int offset, int limit);

    List<UniversityInfo> getUniversityListByLocation(Double lat, Double lng);

    List<UniversityInfo> getUniversityListBasic();
}
