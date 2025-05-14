package JJinBBang.app.domain.building.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import JJinBBang.app.domain.building.document.BuildingKeywordCounts;

public interface BuildingKeywordCountRepository extends MongoRepository<BuildingKeywordCounts, ObjectId> {
	Optional<BuildingKeywordCounts> findByBuildingIdAndIsAgency(Long buildingId, Boolean isAgency);
}
