package JJinBBang.app.domain.common.service;

import JJinBBang.app.domain.common.entity.Universities;
import JJinBBang.app.domain.common.dto.UniversityResponseDto;
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

    private final UniversityRepository universityRepository;

    @Transactional
    @Override
    public List<UniversityResponseDto> getUniversityList(int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by("id").ascending());

        Page<Universities> universityPage = universityRepository.findAll(pageable);

        return universityPage.getContent().stream()
                .map(UniversityResponseDto::of)
                .toList();
    }
}
