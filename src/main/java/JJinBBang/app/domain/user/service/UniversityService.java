package JJinBBang.app.domain.user.service;

import JJinBBang.app.domain.common.entity.Universities;
import JJinBBang.app.domain.user.dto.UniversityResponseDto;
import JJinBBang.app.domain.user.repository.UniversityRepository;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UniversityService {

    private final UniversityRepository universityRepository;

    @Transactional(readOnly = true)
    public ResTemplate<List<UniversityResponseDto>> getUniversityList(int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by("id").ascending());

        Page<Universities> universityPage = universityRepository.findAll(pageable);

        List<UniversityResponseDto> universityList = universityPage.getContent().stream()
                .map(univ -> new UniversityResponseDto(
                        univ.getId(),
                        univ.getUniversityName(),
                        univ.getUniversityLogo()))
                .collect(Collectors.toList());

        return new ResTemplate<>(HttpStatus.OK, "대학교 리스트 조회 성공", universityList);
    }
}
