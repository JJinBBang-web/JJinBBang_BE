package JJinBBang.app.domain.building.entity;

import java.io.Serializable;

import JJinBBang.app.domain.user.entity.Users;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;

@Embeddable
@EqualsAndHashCode
public class BuildingLikeId implements Serializable {
	private Long buildingId;
	private Long userId;
}