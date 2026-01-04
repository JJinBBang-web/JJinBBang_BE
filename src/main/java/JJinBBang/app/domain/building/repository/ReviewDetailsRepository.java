package JJinBBang.app.domain.building.repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import JJinBBang.app.domain.building.entity.ReviewDetails;

@Repository
public interface ReviewDetailsRepository extends MongoRepository<ReviewDetails, ObjectId> {

	Optional<ReviewDetails> findByReviewId(Long reviewId);

    void deleteByReviewId(Long reviewId);

	List<ReviewDetails> findAllByReviewIdIn(List<Long> reviewIds);
}
