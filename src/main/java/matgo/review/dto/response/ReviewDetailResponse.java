package matgo.review.dto.response;

import matgo.member.dto.response.MemberResponse;
import matgo.restaurant.dto.response.RestaurantResponse;

public record ReviewDetailResponse(
  ReviewResponse review,
  MemberResponse member,
  RestaurantResponse restaurant
) {

}
