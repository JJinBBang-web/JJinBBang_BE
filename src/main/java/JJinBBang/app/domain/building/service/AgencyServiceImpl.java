package JJinBBang.app.domain.building.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import JJinBBang.app.domain.building.document.BuildingKeywordCounts;
import JJinBBang.app.domain.building.dto.AgencySearchItem;
import JJinBBang.app.domain.building.dto.AgencySearchRequest;
import JJinBBang.app.domain.building.dto.AgencySearchResponse;
import JJinBBang.app.domain.building.dto.BuildingDetailResponse;
import JJinBBang.app.domain.building.dto.KeywordCount;
import JJinBBang.app.domain.building.dto.VWorldWfsGetFeatureRequest;
import JJinBBang.app.domain.building.dto.VWorldFeatureCollection;
import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.exception.BuildingNullException;
import JJinBBang.app.domain.building.infra.VWorldApiClient;
import JJinBBang.app.domain.building.repository.AgenciesRepository;
import JJinBBang.app.domain.building.repository.BuildingKeywordCountsRepository;
import JJinBBang.app.domain.user.entity.Users;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgencyServiceImpl implements AgencyService{
    private final AgenciesRepository agenciesRepository;
    private final BuildingKeywordCountsRepository buildingKeywordCountRepository;
	private final VWorldApiClient vWorldApiClient;

    @Override
    @Transactional(readOnly = true)
    public BuildingDetailResponse getAgencyDetail(Long buildingId, Users user) {

        // 1) 건물이 없다면 오류
        Agencies agency = agenciesRepository.findById(buildingId).orElseThrow(BuildingNullException::new);

        // 2) 좋아요 여부 계산
        Boolean liked = agency.getAgencyLikes().stream()
                .anyMatch(like -> like.getUser().getUserId().equals(user.getUserId()));

        // 3) 키워드 조회 - 없으면 새 엔티티
        BuildingKeywordCounts buildingKeywordCounts = buildingKeywordCountRepository
                .findByBuildingIdAndIsAgency(buildingId, true)
                .orElseGet(() -> BuildingKeywordCounts.of(buildingId, true));

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

	@Override
	@Transactional(readOnly = true)
	public AgencySearchResponse getAgencyList(AgencySearchRequest request) {

		int num = request.num();
		String cursor = request.cursor();

		VWorldWfsGetFeatureRequest vWorldReq =
			VWorldWfsGetFeatureRequest.of(request.agencyName(), num, cursor);

		VWorldFeatureCollection fc = vWorldApiClient.searchAgencies(vWorldReq);

		if (fc == null || fc.features() == null) {
			return AgencySearchResponse.of(List.of(), num, null, false);
		}

		List<AgencySearchItem> items = fc.features().stream()
			.filter(f -> f.geometry() != null && f.geometry().coordinates() != null && f.geometry().coordinates().size() >= 2 && f.properties() != null)
			.map(f -> {
				var p = f.properties();
				double lon = f.geometry().coordinates().get(0); // [lon, lat]
				double lat = f.geometry().coordinates().get(1);

				return AgencySearchItem.of(
					p.registNo(),
					p.agencyName(),
					p.rdnmadr(),
					p.mnnmadr(),
					lat,
					lon
				);
			})
			.toList();

		// nextCursor = 마지막 아이템의 등록번호
		String nextCursor = items.isEmpty() ? null : items.get(items.size() - 1).registerNumber();

		// hasMore는 “일단 num만큼 꽉 찼으면 다음이 있을 가능성” 정도로 판단
		boolean hasMore = items.size() == num && nextCursor != null;

		return AgencySearchResponse.of(items, num, nextCursor, hasMore);
	}
}
