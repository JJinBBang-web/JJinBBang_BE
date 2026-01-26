package JJinBBang.app.domain.common.service;

import JJinBBang.app.domain.common.entity.Campuses;
import JJinBBang.app.domain.common.entity.Universities;
import JJinBBang.app.domain.common.dto.UniversityResponseDto;
import JJinBBang.app.domain.common.repository.CampusesRepository;
import JJinBBang.app.domain.common.repository.UniversitiesRepository;
import JJinBBang.app.global.common.dto.CampusInfo;
import JJinBBang.app.global.common.dto.UniversityInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UniversityServiceImpl implements UniversityService {

    private final UniversitiesRepository universitiesRepository;
    private final CampusesRepository campusesRepository;

    @Override
    public List<UniversityResponseDto> getUniversityList(int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by("id").ascending());

        Page<Universities> universityPage = universitiesRepository.findAll(pageable);

        return universityPage.getContent().stream()
                .map(UniversityResponseDto::of)
                .toList();
    }

    @Override
    public List<UniversityInfo> getUniversityListByLocation(Double lat, Double lng) {
        List<Object[]> results = campusesRepository.findNearestCampuses(lat, lng);

        return results.stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<UniversityInfo> getUniversityListBasic() {
        List<Object[]> results = campusesRepository.findTop10PopularUniversities();

        return results.stream()
                .map(this::mapToDto)
                .toList();
    }

    private UniversityInfo mapToDto(Object[] row) {
        Long id = ((Number) row[0]).longValue();
        String campusName = (String) row[1];
        String campusAddress = (String) row[2];
        Double campusLat = ((Number) row[3]).doubleValue();
        Double campusLot = ((Number) row[4]).doubleValue();
        String image = (String) row[5];
        String univName = (String) row[6];
        String univLogo = (String) row[7];

        CampusInfo campusInfo = new CampusInfo(
                id, campusName, image, campusAddress, campusLat, campusLot
        );

        return new UniversityInfo(id, univName, univLogo, campusInfo);
    }
}
