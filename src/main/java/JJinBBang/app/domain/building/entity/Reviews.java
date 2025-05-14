package JJinBBang.app.domain.building.entity;

import JJinBBang.app.domain.building.enums.ReviewType;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.STRING)
public class Reviews extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="review_id")
    private Long id; // 리뷰 id

    // DTYPE 컬럼 읽기 전용으로만 맵핑
    @Enumerated(EnumType.STRING)
    @Column(name="DTYPE", insertable = false, updatable = false)
    private ReviewType dtype;

    @Column(name = "likes_count", nullable = false, columnDefinition = "integer default 0")
    private Integer likesCount; // 좋아요 수

    @Column(name = "thumbnail_image", nullable = false, length = 2083)
    private String thumbnailImage; // 썸네일 이미지

    @Column(nullable = false)
    private String content; // 후기 내용

    @Column(length = 30)
    private  String tags; // 태그

    @Column(name = "rating", precision = 3, scale = 2, nullable = false)
    private BigDecimal rating; // 후기 평점

    // 연관관계 매핑
    // 유저 -> 리뷰
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user; // 사용자 id

    // 건물 -> 리뷰
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = true)
    private Buildings building; // 건물 id

    // 공인중개사 -> 리뷰
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = true)
    private Agencies agency; // 공인중개사 id

    // 리뷰 -> 리뷰 좋아요
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL)
    private List<ReviewLikes> reviewLikes = new ArrayList<>();
}
