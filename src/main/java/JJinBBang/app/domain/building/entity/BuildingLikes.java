package JJinBBang.app.domain.building.entity;

import JJinBBang.app.domain.user.entity.Users;
import jakarta.persistence.*;
        import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "building_likes")
//@IdClass(BuildingLikeId.class)
public class BuildingLikes {

    @Id
    @ManyToOne
    @JoinColumn(name = "building_id", nullable = false)
    private Buildings building;

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
}