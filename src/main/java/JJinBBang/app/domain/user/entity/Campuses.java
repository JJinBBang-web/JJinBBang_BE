package JJinBBang.app.domain.user.entity;

import java.awt.geom.Point2D;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
	private Long id;

	@Column(nullable = false, length = 100)
	private String campusName;

	@Column(nullable = false, length = 255)
	private String campusAddress;

	@Column(nullable = false)
	private Point2D.Double campusCoordinate;

	private String image;
}
