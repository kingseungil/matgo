package matgo.post.dto.response;

import java.time.LocalDateTime;

public record PostResponse(
  Long id,
  String title,
  String content,
  int likeCount,
  int dislikeCount,
  int commentCount,
  String regionName,
  LocalDateTime createdAt,
  LocalDateTime modifiedAt
) {

}
