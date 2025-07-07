package JJinBBang.app.domain.building.entity;

import JJinBBang.app.domain.user.entity.Users;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BuildingLikes {
    @EmbeddedId
    private BuildingLikeId id;

    @MapsId("buildingId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Buildings building;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    public static BuildingLikes create(Buildings building, Users user) {
        BuildingLikeId buildingLikeId = BuildingLikeId.of(building.getId(), user.getUserId());
        BuildingLikes buildingLikes = new BuildingLikes();
        buildingLikes.id = buildingLikeId;
        buildingLikes.building = building;
        buildingLikes.user = user;
        return buildingLikes;
    }
}