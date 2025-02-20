package JJinBBang.app.domain.building.entity;

import java.awt.geom.Point2D;

import JJinBBang.app.domain.building.enums.BuildingType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Buildings {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "building_id")
	private Long id; // 건물 id

	@Column(nullable = false)
	private String buildingName; // 건물 이름

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BuildingType buildingType; // 건물 유형

	@Column(nullable = false, length = 100)
	private String addressRoadName; // 건물 도로명 주소

	@Column(nullable = false, length = 100)
	private String addressNumber; // 건물 지번 주소

	@Column(nullable = false)
	private Point2D.Double buildingCoordinate; // 건물 좌표

	private Double area; // 건물 면적

	@Column(nullable = false)
	private Double buildingRating; // 건물 평점

	@Column(nullable = false)
	private Integer reviewCount; // 후기 수

	@Column(nullable = false)
	private Integer likeCount; // 좋아요 수

	@Column(nullable = false)
	private Integer imagesCount; // 이미지 수
}
