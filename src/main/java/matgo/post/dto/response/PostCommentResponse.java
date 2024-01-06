package matgo.post.dto.response;

import java.time.LocalDateTime;

public record PostCommentResponse(
  Long id,
  String content,
  LocalDateTime createdAt,
  LocalDateTime modifiedAt
) {

}
