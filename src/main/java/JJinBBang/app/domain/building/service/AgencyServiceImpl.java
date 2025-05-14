package JJinBBang.app.domain.building.service;

import JJinBBang.app.domain.building.document.BuildingKeywordCounts;
import JJinBBang.app.domain.building.dto.BuildingDetailResponse;
import JJinBBang.app.domain.building.dto.KeywordCount;
import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.exception.BuildingNullException;
import JJinBBang.app.domain.building.repository.AgencyRepository;
import JJinBBang.app.domain.building.repository.BuildingKeywordCountRepository;
import JJinBBang.app.domain.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgencyServiceImpl implements AgencyService{
    private final AgencyRepository agencyRepository;
    private final BuildingKeywordCountRepository buildingKeywordCountRepository;

    @Override
    @Transactional(readOnly = true)
    public BuildingDetailResponse getAgencyDetail(Long buildingId, Users user) {

        // 1) 건물이 없다면 오류
        Agencies agency = agencyRepository.findById(buildingId).orElseThrow(BuildingNullException::new);

        // 2) 좋아요 여부 계산
        Boolean liked = agency.getAgencyLikes().stream()
                .anyMatch(like -> like.getUser().equals(user));

        // 3) 키워드 조회 - 없으면 새 엔티티
        BuildingKeywordCounts buildingKeywordCounts = buildingKeywordCountRepository
                .findByBuildingIdAndIsAgency(buildingId, true)
                .orElseGet(() -> BuildingKeywordCounts.of(buildingId));

        // 4) keywordCounts 조회 - 없으면 새 리스트
        List<KeywordCount> keywordCounts = buildingKeywordCounts.getKeywordCounts();
        if(keywordCounts == null){
            keywordCounts = new ArrayList<>();
        }


        List<KeywordCount> top5Positive = keywordCounts.stream()
                // PO 로 시작하는 키워드만 필터링
                .filter(kc -> kc.key().toString().startsWith("PO"))
                // 리뷰 수 기준 내림차순 정렬
                .sorted(Comparator.comparing(KeywordCount::count).reversed())
                // 상위 5개만
                .limit(5)
                .toList();

        return BuildingDetailResponse.ofAgency(agency, liked, top5Positive);
    }
}
