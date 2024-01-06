package matgo.post.dto.response;

import java.util.List;
import matgo.member.dto.response.MemberResponse;

public record PostDetailResponse(
  PostResponse post,
  List<String> postImages,
  MemberResponse member,
  List<PostCommentResponse> postComments
) {

}
