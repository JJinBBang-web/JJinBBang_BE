package JJinBBang.app.domain.user.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.global.common.enums.Provider;
import JJinBBang.app.global.common.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Users {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long userId;

	@Column(name = "university_id", nullable = false)
	private Long universityId;

	@Enumerated(EnumType.STRING)
	@Column(name = "provider", nullable = false)
	private Provider provider;

	@Column(name = "provider_id", length = 100, nullable = false)
	private String providerId;

	@Column(name = "university_email", length = 255)
	private String universityEmail;

	@Column(name = "admission_certificate", length = 225)
	private String admissionCertificate;

	@Enumerated(EnumType.STRING)
	@Column(name = "verification_status", nullable = false)
	private VerificationStatus verificationStatus;

	@Column(name = "admission_certificate_upload_date")
	private LocalDateTime admissionCertificateUploadDate;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "disabled_at")
	private LocalDateTime disabledAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.verificationStatus = VerificationStatus.UNVERIFIED;
	}

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private List<Reviews> reviews = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "university_id")
	private Universities university;

	//@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	//private List<BuildingLikes> buildingLikes = new ArrayList<>();

	//@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	//private List<ReviewLikes> reviewLikes = new ArrayList<>();
}
