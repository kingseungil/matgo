package matgo.post.dto.response;

import java.util.List;

public record PostCommentSliceResponse(
  List<PostCommentResponse> comments,
  boolean hasNext
) {

}
