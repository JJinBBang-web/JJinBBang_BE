package JJinBBang.app.domain.user.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import JJinBBang.app.domain.building.entity.BuildingLikes;
import JJinBBang.app.domain.building.entity.ReviewLikes;
import JJinBBang.app.domain.building.entity.Reviews;
import JJinBBang.app.domain.common.entity.Universities;
import JJinBBang.app.global.common.enums.Provider;
import JJinBBang.app.global.common.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Users {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long userId;

	@Enumerated(EnumType.STRING)
	@Column(name = "provider", nullable = false)
	private Provider provider;

	@Column(name = "provider_id", length = 100, nullable = false)
	private String providerId;

	@Column(name = "university_email", length = 255)
	private String universityEmail;

	@Column(name = "admission_certificate", length = 225)
	private String admissionCertificate;

	@Column(name = "admission_certificate_upload_date")
	private LocalDateTime admissionCertificateUploadDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "verification_status", nullable = false)
	private VerificationStatus verificationStatus;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "disabled_at")
	private LocalDateTime disabledAt;

	@PrePersist
	protected void onCreate() {
		this.universityEmail = null;
		this.admissionCertificate = null;
		this.admissionCertificateUploadDate = null;
		this.createdAt = LocalDateTime.now();
		this.verificationStatus = VerificationStatus.UNVERIFIED;
		this.disabledAt = null;
	}

	// 연관관계 매핑
	// 대학교 -> 유저
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "university_id")
	private Universities university;

	// 유저 -> 리뷰
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private List<Reviews> reviews = new ArrayList<>();

	// 유저 -> 건물-좋아요
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private List<BuildingLikes> buildingLikes = new ArrayList<>();

	// 유저 -> 리뷰-좋아요
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private List<ReviewLikes> reviewLikes = new ArrayList<>();


	@Builder
	private Users(
		Provider provider,
		String providerId

	){
		this.provider = provider;
		this.providerId = providerId;
		this.universityEmail = null;
		this.admissionCertificate = null;
		this.admissionCertificateUploadDate = null;
		this.createdAt = LocalDateTime.now();
		this.verificationStatus = VerificationStatus.UNVERIFIED;
		this.disabledAt = null;
	}
}
