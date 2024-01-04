package matgo.post.dto.response;

import java.time.LocalDateTime;

public record PostResponse(
  Long postId,
  String title,
  String content,
  int likeCount,
  int dislikeCount,
  String regionName,
  LocalDateTime createdAt,
  LocalDateTime modifiedAt
) {

}
