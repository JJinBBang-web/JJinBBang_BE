package JJinBBang.app.domain.common.service;

import JJinBBang.app.domain.common.entity.Campuses;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CampusesService {
    List<Campuses> findCampuses(Long universityId);
}
