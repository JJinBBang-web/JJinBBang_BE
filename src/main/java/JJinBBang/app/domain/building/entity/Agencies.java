package JJinBBang.app.domain.building.entity;

import JJinBBang.app.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Agencies extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "agency_id")
	private Long agencyId;

	@Column(name = "agency_serial")
	private String agencySerial;

	@Column(name = "name", length = 255, nullable = false)
	private String name;

	@Column(name = "address", length = 255, nullable = false)
	private String address;

	@Column(name = "agency_lat", nullable = false)
	private Double agencyLat;

	@Column(name = "agency_lot", nullable = false)
	private Double agencyLot;

	@Column(name = "rating", precision = 3, scale = 2, nullable = true)
	private BigDecimal rating;

	@Column(name = "like_count", nullable = false)
	private Integer likeCount;

	@Column(name = "review_count", nullable = false)
	private Integer reviewCount;

	@Column(name = "images_count", nullable = false)
	private Integer imagesCount;

	// 공인중개사 -> 공인중개사-좋아요
	@OneToMany(mappedBy = "agency", cascade = CascadeType.ALL)
	@Builder.Default
	private List<AgencyLikes> agencyLikes = new ArrayList<>();

	@OneToMany(mappedBy = "agency", cascade = CascadeType.ALL)
	@Builder.Default
	private List<Reviews> reviews = new ArrayList<>();

	public void incrementReviewCount() {
		this.reviewCount++;
	}

	public void updateAverageRating(BigDecimal newRating) {
		if (this.rating == null) {
			this.rating = newRating;
			return;
		}

		int count = this.reviewCount;

		BigDecimal previousTotal = this.rating
			.multiply(BigDecimal.valueOf(count - 1));

		BigDecimal newTotal = previousTotal.add(newRating);

		this.rating = newTotal
			.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
	}

	/**
	 * 새로운 평점을 추가하고, 리뷰 수와 평균 평점을 갱신합니다.
	 *
	 * @param newRating 새로 추가된 평점
	 */
	public void addRating(BigDecimal newRating) {
		// 1) 기존 총합 계산: oldAvg * oldCount + newRating
		BigDecimal total =
				(this.rating == null ? BigDecimal.ZERO : this.rating.multiply(BigDecimal.valueOf(this.reviewCount)))
						.add(newRating);

		// 2) 리뷰 수 증가
		this.reviewCount++;

		// 3) 평균 평점 재계산 (소수점 둘째 자리 반올림)
		this.rating = total
				.divide(BigDecimal.valueOf(this.reviewCount), 2, RoundingMode.HALF_UP);
	}

	/**
	 * 기존 평점을 제거하고, 리뷰 수와 평균 평점을 갱신합니다.
	 *
	 * @param oldRating 제거하려는 평점
	 */
	public void removeRating(BigDecimal oldRating) {
		if (this.reviewCount <= 1) {
			// 리뷰가 하나뿐이거나 0이 될 경우 초기화
			this.reviewCount = 0;
			this.rating = null;
		} else {
			// 1) 총합에서 oldRating 제거
			BigDecimal total = this.rating
					.multiply(BigDecimal.valueOf(this.reviewCount))
					.subtract(oldRating);

			// 2) 리뷰 수 감소
			this.reviewCount--;

			// 3) 평균 평점 재계산
			this.rating = total
					.divide(BigDecimal.valueOf(this.reviewCount), 2, RoundingMode.HALF_UP);
		}
	}

	/**
	 * 기존 평점을 새로운 평점으로 교체하고, 평균 평점을 재계산합니다.
	 *
	 * @param oldRating 교체 전 평점
	 * @param newRating 교체 후 평점
	 */
	public void updateRating(BigDecimal oldRating, BigDecimal newRating) {
		// 1) 총합에서 oldRating 제거 후 newRating 추가
		BigDecimal total = this.rating
				.multiply(BigDecimal.valueOf(this.reviewCount))
				.subtract(oldRating)
				.add(newRating);

		// 2) 평균 평점 재계산 (리뷰 수 변동 없음)
		this.rating = total
				.divide(BigDecimal.valueOf(this.reviewCount), 2, RoundingMode.HALF_UP);
	}

	/**
	 * 이미지 수를 1 증가시킵니다.
	 */
	public void incrementImagesCount() {
		this.imagesCount++;
	}

	/**
	 * 이미지 수를 1 감소시킵니다.
	 * 0 미만으로 내려가지 않도록 방어 코드 포함
	 */
	public void decrementImagesCount() {
		if (this.imagesCount > 0) {
			this.imagesCount--;
		}
	}

	public void incrementLikeCount() {
		if (this.likeCount == null) {
			this.likeCount = 0;
		}
		this.likeCount++;
	}

	public void decrementLikeCount() {
		if (this.likeCount > 0) {
			this.likeCount--;
		}
	}
}
