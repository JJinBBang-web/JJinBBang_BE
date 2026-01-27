package JJinBBang.app.domain.common.service;

import JJinBBang.app.domain.common.dto.CampusSearchResponse;
import JJinBBang.app.domain.common.entity.Universities;
import JJinBBang.app.domain.common.dto.UniversityResponseDto;
import JJinBBang.app.domain.common.repository.CampusesRepository;
import JJinBBang.app.domain.common.repository.UniversitiesRepository;
import JJinBBang.app.global.common.dto.CampusInfo;
import JJinBBang.app.global.common.dto.UniversityInfo;
import JJinBBang.app.domain.user.repository.UniversityRepository;
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

    private static final int SEARCH_LIMIT_COUNT = 10;

    private final UniversitiesRepository universitiesRepository;
    private final UniversityRepository universityRepository;
    private final CampusesRepository campusesRepository;
  
    @Transactional
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
        List<Object[]> results = campusesRepository.findNearestCampuses(
                lat, lng, CampusesRepository.EARTH_RADIUS, SEARCH_LIMIT_COUNT
        );
        return results.stream().map(this::mapToDto).toList();
    }

    @Override
    public List<UniversityInfo> getUniversityListBasic() {
        List<Object[]> results = campusesRepository.findTopPopularUniversities(SEARCH_LIMIT_COUNT);
        return results.stream().map(this::mapToDto).toList();
    }

    private UniversityInfo mapToDto(Object[] row) {
        Long campusId = ((Number) row[0]).longValue();
        Long universityId = ((Number) row[1]).longValue();

        String campusName = (String) row[2];
        String campusAddress = (String) row[3];
        Double campusLat = ((Number) row[4]).doubleValue();
        Double campusLot = ((Number) row[5]).doubleValue();
        String image = (String) row[6];
        String univName = (String) row[7];
        String univLogo = (String) row[8];

        CampusInfo campusInfo = new CampusInfo(
                campusId, campusName, image, campusAddress, campusLat, campusLot
        );

        return new UniversityInfo(
                universityId, univName, univLogo, campusInfo
        );
    }
  
    @Transactional(readOnly = true)
    public CampusSearchResponse searchCampuses(String query, int limit, int offset) {
        String kw = query.replaceAll("\\s+", "");
        limit = Math.min(Math.max(limit, 1), 50);
        offset = Math.max(offset, 0);


        List<CampusSearchResponse.CampusSearchItem> items;
        if (kw.isEmpty()) {
            items = List.of();
        }
        else {
            items = campusesRepository.search(kw, limit, offset).stream()
                .map(r -> CampusSearchResponse.CampusSearchItem.of(
                    r.getUniversityName() + " " + r.getCampusName(),
                    r.getCampusAddress(),
                    r.getCampusId(),
                    r.getCampusName(),
                    r.getUniversityId(),
                    r.getUniversityName()
                ))
                .toList();
        }

        return CampusSearchResponse.of(items, limit, offset);
    }
}
