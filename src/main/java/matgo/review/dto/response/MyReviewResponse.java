package matgo.review.dto.response;

import matgo.restaurant.dto.response.RestaurantResponse;

public record MyReviewResponse(
  ReviewResponse review,
  RestaurantResponse restaurant
) {

}
