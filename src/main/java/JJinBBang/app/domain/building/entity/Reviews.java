package JJinBBang.app.domain.building.entity;

import JJinBBang.app.domain.building.enums.BuildingType;
import JJinBBang.app.domain.building.enums.ReviewType;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.common.enums.KeywordType;
import JJinBBang.app.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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

    // 공인중개사 이미지가 없을 수 있기 때문에 썸네일을 nullable로 설정함
    @Column(name = "thumbnail_image", nullable = true, length = 2083)
    private String thumbnailImage; // 썸네일 이미지

    @Column(nullable = false)
    private String content; // 후기 내용

    @Column(length = 70)
    private String tags; // 태그

    @Column(name = "rating", precision = 3, scale = 2, nullable = true)
    private BigDecimal rating; // 후기 평점

    @Enumerated(EnumType.STRING)
    @Column(name = "building_type", nullable = false)
    private BuildingType buildingType;

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

    public List<KeywordType> getTags() {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tags.split(","))
            .map(x->KeywordType.valueOf(x.trim())).toList();
    }

    public void setTags(List<KeywordType> tags) {
        this.tags = String.join(",", tags.stream().map(KeywordType::toString).toList());
    }

    // Reviews.java
    protected Reviews(Long id, ReviewType dtype, Integer likesCount,
                      String thumbnailImage, String content,
                      List<KeywordType> tags,
                      BigDecimal rating, BuildingType buildingType, Users user,
                      Buildings building, Agencies agency,
                      List<ReviewLikes> reviewLikes) {
        this.id             = id;
        this.dtype          = dtype;
        this.likesCount     = likesCount;
        this.thumbnailImage = thumbnailImage;
        this.content        = content;
        setTags(tags == null ? List.of() : tags);
        this.rating         = rating;
        this.buildingType   = buildingType;
        this.user           = user;
        this.building       = building;
        this.agency         = agency;
        this.reviewLikes    = (reviewLikes == null) ? new ArrayList<>() : reviewLikes;
    }
}
