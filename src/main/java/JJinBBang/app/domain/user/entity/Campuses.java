package JJinBBang.app.domain.user.entity;

import java.awt.geom.Point2D;
import java.util.List;

import JJinBBang.app.domain.building.entity.Buildings;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Campuses {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "campus_id")
	private Long id; // 캠퍼스 id

	@Column(nullable = false, length = 100)
	private String campusName; // 캠퍼스 이름

	@Column(nullable = false, length = 255)
	private String campusAddress; // 캠퍼스 주소

	@Column(nullable = false)
	private Point2D.Double campusCoordinate; // 캠퍼스 좌표

	private String image; // 캠퍼스 이미지

	// 연관관계 매핑
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "university_id")
	private Universities university; // 대학교

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "building_id")
	private Buildings building; // 건물
}
