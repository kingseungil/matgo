package matgo.post.dto.request;

import static matgo.global.util.DtoValidator.EMPTY_MESSAGE;

import jakarta.validation.constraints.NotBlank;
import matgo.member.domain.entity.Member;
import matgo.post.domain.entity.Post;
import matgo.post.domain.entity.PostComment;

public record PostCommentCreateRequest(
  @NotBlank(message = EMPTY_MESSAGE)
  String content
) {

    public static PostComment toEntity(Post post, Member member, PostCommentCreateRequest request) {
        return PostComment.builder()
                          .content(request.content())
                          .post(post)
                          .member(member)
                          .build();
    }

}
