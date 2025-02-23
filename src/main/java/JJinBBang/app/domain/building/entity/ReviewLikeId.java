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
public class ReviewLikeId implements Serializable {
	private Long reviewId;
	private Long userId;
}