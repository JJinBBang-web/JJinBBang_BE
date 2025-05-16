package JJinBBang.app.domain.building.dto;

import JJinBBang.app.domain.building.entity.BuildingLikes;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BuildingLikesDTO {
    private Long buildingId;
    private Long userId;

    public static BuildingLikesDTO fromEntity(BuildingLikes buildingLikes) {
        return new BuildingLikesDTO(
                buildingLikes.getBuilding().getId(),
                buildingLikes.getUser().getUserId()
        );
    }
}

