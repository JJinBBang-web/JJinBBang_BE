package JJinBBang.app.domain.building.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import JJinBBang.app.domain.building.dto.BuildingDetailResponse;
import JJinBBang.app.domain.building.dto.KeywordCount;
import JJinBBang.app.domain.building.entity.BuildingLikes;
import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.exception.BuildingNullException;
import JJinBBang.app.domain.building.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BuildingServiceImpl implements BuildingService{
	private final BuildingRepository buildingRepository;


	@Override
	@Transactional(readOnly = true)
	public BuildingDetailResponse getBuildingDetail(Long buildingId, Long userId) {
		Buildings building = buildingRepository.findById(buildingId).orElseThrow(BuildingNullException::new);

		List<BuildingLikes> buildingLike = building.getBuildingLikes();
		Boolean liked = buildingLike.stream()
			.anyMatch(like -> like.getUser().getUserId().equals(userId));

		//TODO 건물에 대한 키워드 상위 5개는 몽고 DB 설정 후 구현
		List<KeywordCount> keywords = new ArrayList<>();

		return BuildingDetailResponse.of(building, liked, keywords);
	}
}
