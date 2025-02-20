package JJinBBang.app.domain.building.entity;

import JJinBBang.app.domain.building.enums.ContractType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reviews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="review_id")
    private Long id;

    @Column(length = 100)
    private String buildingName;

    private Integer unit;

    private Integer floor;

    @Column(nullable = false)
    private ContractType contract_type;

    private Integer deposit;

    private Integer price;

    private Integer maintenance_cost;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime created_at;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updated_at;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer likes_count;

    @Column(nullable = false,length = 2083)
    private String thumbnail_image;

    @Column(nullable = false)
    private String content;

    @Column(length = 30)
    private  String tags;

}
