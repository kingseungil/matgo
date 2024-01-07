package matgo.post.dto.response;

import java.util.List;

public record MyPostSliceResponse(
  List<PostResponse> posts,
  boolean hasNext
) {

}
