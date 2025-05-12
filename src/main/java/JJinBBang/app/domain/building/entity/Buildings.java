package JJinBBang.app.domain.building.entity;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import JJinBBang.app.domain.building.enums.BuildingType;
import JJinBBang.app.domain.common.entity.Campuses;
import JJinBBang.app.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Buildings extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "building_id")
	private Long id; // 건물 id

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

	private Double area; // 건물 면적

	@Column(nullable = false)
	private Double buildingRating; // 건물 평점

	@Column(nullable = false)
	private Integer reviewCount; // 후기 수

	@Column(nullable = false)
	private Integer likeCount; // 좋아요 수

	@Column(nullable = false)
	private Integer imagesCount; // 이미지 수

	// 연관관계 매핑
	// 캠퍼스 -> 건물
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "campus_id")
	private Campuses campus;

	// 건물 -> 건물-좋아요
	@OneToMany(mappedBy = "building", cascade = CascadeType.ALL)
	private List<BuildingLikes> buildingLikes = new ArrayList<>();

	// 건물 -> 리뷰
	@OneToMany(mappedBy = "building", cascade = CascadeType.ALL)
	private List<Reviews> reviews = new ArrayList<>();
}
