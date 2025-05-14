package JJinBBang.app.domain.building.entity;

import JJinBBang.app.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Agencies extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "agency_id")
	private Long id;

	@Column(name = "agency_serial")
	private Long agencySerial;

	@Column(name = "name", length = 255, nullable = false)
	private String name;

	@Column(name = "address", length = 255, nullable = false)
	private String address;

	@Column(name = "agency_lat", nullable = false)
	private Double agencyLat;

	@Column(name = "agency_lot", nullable = false)
	private Double agencyLot;

	@Column(name = "rating", precision = 3, scale = 2, nullable = false)
	private BigDecimal rating;

	@Column(name = "like_count", nullable = false)
	private Integer likeCount;

	@Column(name = "review_count", nullable = false)
	private Integer reviewCount;

	@Column(name = "images_count", nullable = false)
	private Integer imagesCount;

	// 공인중개사 -> 공인중개사-좋아요
	@OneToMany(mappedBy = "agency", cascade = CascadeType.ALL)
	private List<AgencyLikes> agencyLikes = new ArrayList<>();

	@OneToMany(mappedBy = "agency", cascade = CascadeType.ALL)
	private List<Reviews> reviews = new ArrayList<>();
}