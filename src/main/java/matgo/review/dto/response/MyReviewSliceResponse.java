package matgo.review.dto.response;

import java.util.List;

public record MyReviewSliceResponse(
  List<MyReviewResponse> reviews,
  boolean hasNext
) {

}
