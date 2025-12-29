package JJinBBang.app.domain.common.entity;

import JJinBBang.app.domain.common.enums.ReportCategory;
import JJinBBang.app.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reports extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id; // 리포트 id

    @Column(name = "cover_image", nullable = false, length = 2083)
    private String coverImage; // 대표 이미지

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ReportCategory category; // 리포트 분류 (부동산, 자취꿀팁, ...)

    @Column(name = "title", nullable = false, length = 400)
    private String title; // 리포트 제목

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content; // 리포트 본문

    @Column(name = "share_count", nullable = false)
    private int shareCount = 0; // 공유 수

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0; // 좋아요 수

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0; // 조회 수

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL)
    private List<ReportLikes> reportLikes = new ArrayList<>();

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseShareCount() {
        this.shareCount++;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) this.likeCount--;
    }

    public static Reports create(String coverImage, ReportCategory category, String title, String content) {
        Reports report = new Reports();
        report.coverImage = coverImage;
        report.category = category;
        report.title = title;
        report.content = content;
        return report;
    }
}
