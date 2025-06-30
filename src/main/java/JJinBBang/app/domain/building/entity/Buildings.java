package JJinBBang.app.domain.building.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import JJinBBang.app.domain.building.enums.BuildingType;
import JJinBBang.app.domain.common.entity.Campuses;
import JJinBBang.app.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Buildings extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "building_id")
	private Long id; // 건물 id

	@Column(nullable = false, unique = true)
	private String buildingCode; // 카카오 건물 관리 번호

	@Column(nullable = false)
	private String buildingName; // 건물 이름

	@Column(nullable = false)
	private String buildingType; // 건물 유형

	@Column(nullable = false, length = 255)
	private String buildingAddress; // 건물 주소

	@Column(nullable = false)
	private Double buildingLat; // 건물 위도

	@Column(nullable = false)
	private Double buildingLot; // 건물 경도

	@Column(name = "rating", precision = 3, scale = 2, nullable = true)
	private BigDecimal buildingRating; // 건물 평점

	@Column(nullable = false)
	private Integer reviewCount; // 후기 수

	@Column(nullable = false)
	private Integer likeCount; // 좋아요 수

	@Column(nullable = false)
	private Integer imagesCount; // 이미지 수

	// 연관관계 매핑
	// 캠퍼스 -> 건물
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "campus_id", nullable = true)
	private Campuses campus;

	// 건물 -> 건물-좋아요
	@OneToMany(mappedBy = "building", cascade = CascadeType.ALL)
	@Builder.Default
	private List<BuildingLikes> buildingLikes = new ArrayList<>();

	// 건물 -> 리뷰
	@OneToMany(mappedBy = "building", cascade = CascadeType.ALL)
	@Builder.Default
	private List<Reviews> reviews = new ArrayList<>();

	/**
	 * 저장된 buildingType 문자열을 파싱하여 enum 리스트로 반환합니다.
	 *
	 * @return BuildingType enum 리스트
	 */
	public List<BuildingType> getBuildingType() {
		return Arrays.stream(buildingType.split(","))
				.map(String::trim)
				.map(BuildingType::valueOf)
				.toList();
	}

	public void incrementReviewCount() {
		this.reviewCount++;
	}

	public void updateAverageRating(BigDecimal newRating) {
		if (this.buildingRating == null) {
			this.buildingRating = newRating;
			return;
		}

		int count = this.reviewCount;

		BigDecimal previousTotal = this.buildingRating
			.multiply(BigDecimal.valueOf(count - 1));

		BigDecimal newTotal = previousTotal.add(newRating);

		this.buildingRating = newTotal
			.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
	}


	/**
	 * enum 리스트를 쉼표 구분 문자열로 변환하여 buildingType 필드를 설정합니다.
	 *
	 * @param buildingTypeList BuildingType enum 리스트
	 */
	public void setBuildingType(List<BuildingType> buildingTypeList) {
		this.buildingType = buildingTypeList.stream()
				.map(BuildingType::toString)
				.collect(Collectors.joining(","));
	}

	/**
	 * 새로운 평점을 추가하고, 리뷰 수와 평균 평점을 갱신합니다.
	 *
	 * @param newRating 추가할 평점
	 */
	public void addRating(BigDecimal newRating) {
		// 기존 평균 * 기존 개수 + 새 평점
		BigDecimal total = (this.buildingRating == null
				? BigDecimal.ZERO
				: this.buildingRating.multiply(BigDecimal.valueOf(this.reviewCount)))
				.add(newRating);

		// 리뷰 수 증가
		this.reviewCount++;

		// 새 평균 계산 (소수점 2자리, 반올림)
		this.buildingRating = total.divide(
				BigDecimal.valueOf(this.reviewCount),
				2, RoundingMode.HALF_UP);
	}

	/**
	 * 기존 평점을 제거하고, 리뷰 수와 평균 평점을 갱신합니다.
	 *
	 * @param oldRating 제거할 평점
	 */
	public void removeRating(BigDecimal oldRating) {
		if (this.reviewCount <= 1) {
			// 리뷰가 1개 이하이면 초기 상태로
			this.reviewCount = 0;
			this.buildingRating = null;
		} else {
			// 총합 계산 후 oldRating 제외
			BigDecimal total = this.buildingRating
					.multiply(BigDecimal.valueOf(this.reviewCount))
					.subtract(oldRating);

			// 리뷰 수 감소
			this.reviewCount--;

			// 평균 재계산
			this.buildingRating = total.divide(
					BigDecimal.valueOf(this.reviewCount),
					2, RoundingMode.HALF_UP);
		}
	}

	/**
	 * 기존 평점을 새 평점으로 업데이트하고, 평균 평점을 재계산합니다.
	 *
	 * @param oldRating 교체 전 평점
	 * @param newRating 교체 후 평점
	 */
	public void updateRating(BigDecimal oldRating, BigDecimal newRating) {
		// 총합에서 oldRating 제거 후 newRating 추가
		BigDecimal total = this.buildingRating
				.multiply(BigDecimal.valueOf(this.reviewCount))
				.subtract(oldRating)
				.add(newRating);

		// 평균 재계산 (리뷰 수 변동 없음)
		this.buildingRating = total.divide(
				BigDecimal.valueOf(this.reviewCount),
				2, RoundingMode.HALF_UP);
	}

	/**
	 * 이미지 수를 1 증가시킵니다.
	 */
	public void incrementImagesCount() {
		this.imagesCount++;
	}

	/**
	 * 이미지 수를 1 감소시키되, 0 미만으로 내려가지 않도록 방어 처리합니다.
	 */
	public void decrementImagesCount() {
		if (this.imagesCount > 0) {
			this.imagesCount--;
		}
	}
}
