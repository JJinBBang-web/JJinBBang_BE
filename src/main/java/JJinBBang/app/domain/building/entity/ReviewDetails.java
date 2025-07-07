package JJinBBang.app.domain.building.entity;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import JJinBBang.app.domain.building.enums.BuildingType;
import JJinBBang.app.global.common.dto.Keywords;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
@Document(collection = "review_details")
public class ReviewDetails {
	@Id
	private ObjectId id;

	@Field(name = "review_id")
	private Long reviewId;

	@Field(name = "building_id")
	private Long buildingId;

	@Field(name = "building_type")
	private BuildingType buildingType;

	private List<String> images;

	@Field(name = "image_count")
	private Integer imageCount;

	private Keywords keywords;
}
