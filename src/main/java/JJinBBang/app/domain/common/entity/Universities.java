package JJinBBang.app.domain.common.entity;

import java.util.ArrayList;
import java.util.List;

import JJinBBang.app.domain.user.entity.Users;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Universities {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "university_id")
	private Long id; // 대학교 id

	@Column(nullable = false, length = 100)
	private String universityName; // 대학교 이름

	@Column(nullable = false)
	private String universityLogo; // 대학교 로고

	// 연관관계 매핑
	// 대학교 -> 캠퍼스
	@OneToMany(mappedBy = "universities", cascade = CascadeType.ALL)
	private List<Campuses> campuses = new ArrayList<>(); // 캠퍼스

	// 대학교 -> 유저
	@OneToMany(mappedBy = "universities", cascade = CascadeType.ALL)
	private List<Users> users= new ArrayList<>(); // 유저
}