package JJinBBang.app.domain.building.entity;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BuildingLikeId implements Serializable {
	private Long buildingId;
	private Long userId;

	public static BuildingLikeId of(Long buildingId, Long userId) {
		if (buildingId == null || userId == null) {
			throw new IllegalArgumentException("BuildingId와 UserId는 null이 될 수 없습니다.");
		}
		return new BuildingLikeId(buildingId, userId);
	}
}