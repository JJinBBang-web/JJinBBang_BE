package JJinBBang.app.domain.building.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import JJinBBang.app.domain.building.entity.ReviewDetails;

public interface ReviewDetailRepository extends MongoRepository<ReviewDetails, ObjectId> {
	Optional<ReviewDetails> findByReviewId(Long reviewId);
}
