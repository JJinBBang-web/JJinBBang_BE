package JJinBBang.app.domain.building.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "agency_reviews")
@PrimaryKeyJoinColumn(name = "review_id")
public class AgencyReviews extends Reviews {
}