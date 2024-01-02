package matgo.review.dto.response;

import java.util.List;

public record ReviewSliceResponse(
  List<ReviewResponse> reviews,
  boolean hasNext
) {

}
