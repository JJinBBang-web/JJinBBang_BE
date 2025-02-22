package JJinBBang.app.domain.building.entity;

import JJinBBang.app.domain.building.enums.ContractType;
import JJinBBang.app.domain.user.entity.Universities;
import JJinBBang.app.domain.user.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reviews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="review_id")
    private Long id; // 리뷰 id

    @Column(length = 100)
    private String block; // 동

    private Integer unit; // 호

    private Integer floor; // 층

    @Column(nullable = false)
    private ContractType contract_type; // 계약 형태

    private Integer deposit; // 보증금

    private Integer price; // 월세

    private Integer maintenance_cost; // 관리비

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime created_at; // 작성일

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updated_at; // 수정일

    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer likes_count; // 좋아요 수

    @Column(nullable = false,length = 2083)
    private String thumbnail_image; // 썸네일 이미지

    @Column(nullable = false)
    private String content; // 후기 내용

    @Column(length = 30)
    private  String tags; // 태그

    // 연관관계 매핑
    // 유저 -> 리뷰
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user; // 사용자 id

    // 건물 -> 리뷰
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Buildings buildings; // 건물 id

    // 대학교 -> 리뷰
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = false)
    private Universities university; // 대학교 id

    // 리뷰 -> 리뷰 좋아요
    @OneToMany(mappedBy = "reviews", cascade = CascadeType.ALL)
    private List<ReviewLikes> reviewLikes = new ArrayList<>();

}
