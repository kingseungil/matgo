package matgo.review.dto.response;

import matgo.member.dto.response.MemberResponse;
import matgo.restaurant.dto.response.RestaurantResponse;

public record ReviewResponse(
  Long reviewId,
  String content,
  int rating,
  String imageUrl,
  boolean revisit,
  MemberResponse member,
  RestaurantResponse restaurant
) {

}