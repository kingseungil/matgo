package matgo.post.dto.response;

import matgo.member.dto.response.MemberResponse;

public record PostListResponse(
  PostResponse post,
  MemberResponse member

) {

}
