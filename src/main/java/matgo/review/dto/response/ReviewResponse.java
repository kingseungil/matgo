package matgo.review.dto.response;

import java.time.LocalDateTime;
import matgo.member.dto.response.MemberResponse;
import matgo.restaurant.dto.response.RestaurantResponse;

public record ReviewResponse(
  Long reviewId,
  String content,
  int rating,
  String imageUrl,
  boolean revisit,
  int likeCount,
  int dislikeCount,
  LocalDateTime createdAt,
  MemberResponse member,
  RestaurantResponse restaurant
) {

}