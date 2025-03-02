package JJinBBang.app.domain.common.service;

import JJinBBang.app.domain.common.entity.Campuses;
import JJinBBang.app.domain.common.exception.CampusNotFoundGroupException;
import JJinBBang.app.domain.common.repository.CampusesRepository;
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

    @Override
    @Transactional
    public List<Campuses> findCampuses(Long universityId) {
        List<Campuses> campusList = campusesRepository.findByUniversityId(universityId);

        if (campusList == null || campusList.isEmpty()) {
            throw new CampusNotFoundGroupException("정보가 존재하지 않습니다.");
        }

        return campusList;
    }

}
