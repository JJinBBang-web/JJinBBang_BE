package JJinBBang.app.domain.building.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import JJinBBang.app.domain.building.document.BuildingKeywordCounts;
import JJinBBang.app.domain.building.dto.AgencySearchItem;
import JJinBBang.app.domain.building.dto.AgencySearchRequest;
import JJinBBang.app.domain.building.dto.AgencySearchResponse;
import JJinBBang.app.domain.building.dto.BuildingDetailResponse;
import JJinBBang.app.domain.building.dto.KeywordCount;
import JJinBBang.app.domain.building.dto.VWorldAddressToCoordRequest;
import JJinBBang.app.domain.building.dto.VWorldAddressToCoordResponse;
import JJinBBang.app.domain.building.dto.VWorldEBOfficeRequest;
import JJinBBang.app.domain.building.dto.VWorldEBOfficeResponse;
import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.exception.AgencyInternalServerErrorException;
import JJinBBang.app.domain.building.exception.AgencyServiceUnavailableException;
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
	private final Executor geocodeExecutor;

    @Override
    @Transactional(readOnly = true)
    public BuildingDetailResponse getAgencyDetail(Long buildingId, Users user) {

        // 1) 건물이 없다면 오류
        Agencies agency = agenciesRepository.findById(buildingId).orElseThrow(BuildingNullException::new);

        // 2) 좋아요 여부 계산
        Boolean liked = agency.getAgencyLikes().stream()
                .anyMatch(like -> like.getUser().equals(user));

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
	public AgencySearchResponse getAgencyList(AgencySearchRequest request) {
		// VWorld API 에서 중개사무소 정보 조회
		VWorldEBOfficeRequest vWorldRequest = VWorldEBOfficeRequest.of(request.agencyName(), request.num(),
			request.page());
		VWorldEBOfficeResponse vWorldResponse = vWorldApiClient.searchAgencies(vWorldRequest);

		VWorldEBOfficeResponse.EDOffices edOffices = vWorldResponse.edOffices();
		// 중개사무소 정보가 없으면 빈 리스트 반환
		if (edOffices == null) {
			return AgencySearchResponse.of(new ArrayList<>(), request.num(), request.page(), 0);
		}
		// 각 중개사무소의 주소를 좌표로 변환하고 AgencySearchItem 생성
		List<AgencySearchItem> items = getAgencySearchItems(edOffices);

		return AgencySearchResponse.of(items, edOffices.numOfRows(), edOffices.pageNo(), edOffices.totalCount());
	}

	private List<AgencySearchItem> getAgencySearchItems(VWorldEBOfficeResponse.EDOffices edOffices) {
		// 비동기 작업을 사용하여 각 중개사무소의 주소를 좌표로 변환
		List<CompletableFuture<AgencySearchItem>> futures;
		try {
			futures = edOffices.field().stream()
				.map(field -> CompletableFuture.supplyAsync(() -> {
					// 지번주소를 좌표로 변환하는 VWorld API 호출
					VWorldAddressToCoordRequest addressToCoordRequest = VWorldAddressToCoordRequest.parcel(
						field.mnnmadr());
					VWorldAddressToCoordResponse addressToCoordResponse = vWorldApiClient.geocode(
						addressToCoordRequest);

					// AgencySearchItem 생성
					return AgencySearchItem.of(
						field.jurirno(),
						field.bsnmCmpnm(),
						field.brkrNm(),
						field.rdnmadr(),
						field.mnnmadr(),
						addressToCoordResponse.response().result().point().x(),
						addressToCoordResponse.response().result().point().y()
					);
				}, geocodeExecutor))
				.toList();
		} catch (RejectedExecutionException e) {
			// Executor가 작업을 수락하지 못하는 경우 처리
			throw AgencyServiceUnavailableException.getAgencyServiceOverloaded();
		}

		List<AgencySearchItem> items;
		try {
			// 모든 비동기 작업이 완료될 때까지 대기하고 결과 수집
			items = futures.stream()
				.map(CompletableFuture::join)
				.toList();
		} catch (CompletionException e) {
			// 비동기 작업 중 예외가 발생한 경우 처리
			throw AgencyInternalServerErrorException.getAgencyInternalServerError();
		}
		return items;
	}
}
