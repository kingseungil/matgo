package matgo.review.dto.response;

import java.time.LocalDateTime;

public record ReviewResponse(
  Long id,
  String content,
  int rating,
  String imageUrl,
  boolean revisit,
  int likeCount,
  int dislikeCount,
  LocalDateTime createdAt

) {

}