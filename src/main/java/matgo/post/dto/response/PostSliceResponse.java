package matgo.post.dto.response;

import java.util.List;

public record PostSliceResponse(
  List<PostListResponse> posts,
  boolean hasNext
) {

}
