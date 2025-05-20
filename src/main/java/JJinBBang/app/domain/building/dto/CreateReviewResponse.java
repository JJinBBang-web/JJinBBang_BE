package JJinBBang.app.domain.building.dto;



public record CreateReviewResponse(
	Long reviewId
) {
	public static CreateReviewResponse from(Long reviewId){
		return new CreateReviewResponse(reviewId);
	}
}
