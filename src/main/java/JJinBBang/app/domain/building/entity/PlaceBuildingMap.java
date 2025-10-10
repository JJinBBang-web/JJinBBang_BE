package JJinBBang.app.domain.building.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceBuildingMap {

	@Id
	@Column(name = "kakao_place_id", length = 64, nullable = false)
	private String kakaoPlaceId;

	@Column(name = "kakao_lat", nullable = false, precision = 20, scale = 15)
	private BigDecimal kakaoLat;

	@Column(name = "kakao_lng", nullable = false, precision = 20, scale = 15)
	private BigDecimal kakaoLng;

	@Column(name = "building_mgmt_no")
	private String buildingMgmtNo; // 발견 시 채움, 미발견이면 NULL

	@Column(name = "manual_override", nullable = false)
	private boolean manualOverride; // 수동 수정 여부 (TINYINT(1) ↔ boolean)

	// DB의 DEFAULT CURRENT_TIMESTAMP를 쓰면서도, 엔티티에선 값 자동 세팅
	@CreationTimestamp
	@Column(
		name = "created_at",
		nullable = false,
		updatable = false
	)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(
		name = "updated_at",
		nullable = false
	)
	private LocalDateTime updatedAt;

	/** 편의 메서드: 한 달 경과 여부 체크 (lazy/배치 재검증에 활용 가능) */
	public boolean needsMonthlyRecheck() {
		return updatedAt == null || updatedAt.isBefore(LocalDateTime.now().minusMonths(1));
	}

	public void updateBuildingMgmtNo(String buildingMgmtNo) {
		this.buildingMgmtNo = buildingMgmtNo;
	}

	public static PlaceBuildingMap of (
		String kakaoPlaceId,
		Double latitude,
		Double longitude,
		String buildingMgmtNo,
		boolean manualOverride
	) {
		return PlaceBuildingMap.builder()
			.kakaoPlaceId(kakaoPlaceId)
			.kakaoLat(BigDecimal.valueOf(latitude))
			.kakaoLng(BigDecimal.valueOf(longitude))
			.buildingMgmtNo(buildingMgmtNo)
			.manualOverride(manualOverride)
			.build();
	}
}
