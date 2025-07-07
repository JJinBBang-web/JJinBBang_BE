package JJinBBang.app.domain.building.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import JJinBBang.app.domain.building.document.BuildingKeywordCounts;

@Repository
public interface BuildingKeywordCountsRepository extends MongoRepository<BuildingKeywordCounts, ObjectId> {
	Optional<BuildingKeywordCounts> findByBuildingIdAndIsAgency(Long buildingId, Boolean isAgency);
}
