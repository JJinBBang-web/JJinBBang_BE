package JJinBBang.app.domain.common.service;

import JJinBBang.app.domain.common.entity.Campuses;
import JJinBBang.app.domain.common.entity.Universities;
import JJinBBang.app.domain.common.exception.CampusNotFoundGroupException;
import JJinBBang.app.domain.common.exception.UniversitiesNotFoundGroupException;
import JJinBBang.app.domain.common.repository.CampusesRepository;
import JJinBBang.app.domain.common.repository.UniversitiesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampusesServiceImpl implements CampusesService {

    private final CampusesRepository campusesRepository;
    private final UniversitiesRepository universitiesRepository;

    @Override
    @Transactional
    public List<Campuses> findCampuses(String universityName) {
        Universities university = universitiesRepository.findByUniversityName(universityName)
                .orElseThrow(() -> new UniversitiesNotFoundGroupException("해당 대학을 찾을 수 없습니다: " + universityName));
        List<Campuses> campusList = campusesRepository.findByUniversityId(university.getId());

        if (campusList == null || campusList.isEmpty()) {
            throw new CampusNotFoundGroupException("정보가 존재하지 않습니다.");
        }

        return campusList;
    }

}
