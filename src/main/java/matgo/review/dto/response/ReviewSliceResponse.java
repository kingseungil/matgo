package matgo.review.dto.response;

import java.util.List;

public record ReviewSliceResponse(
  List<ReviewDetailResponse> reviews,
  boolean hasNext
) {

}
