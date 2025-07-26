package JJinBBang.app.domain.common.service;

import JJinBBang.app.domain.common.dto.UniversityResponseDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UniversityService {

    @Transactional
    List<UniversityResponseDto> getUniversityList(int offset, int limit);

}
