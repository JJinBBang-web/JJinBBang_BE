package JJinBBang.app.domain.building.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "agency_reviews")
@PrimaryKeyJoinColumn(name = "review_id")
@DiscriminatorValue("AGENCY")
public class AgencyReviews extends Reviews {
}