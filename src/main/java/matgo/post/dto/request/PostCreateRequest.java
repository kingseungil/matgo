package matgo.post.dto.request;

import static matgo.global.util.DtoValidator.EMPTY_MESSAGE;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import matgo.member.domain.entity.Member;
import matgo.post.domain.entity.Post;

public record PostCreateRequest(

  @NotBlank(message = EMPTY_MESSAGE)
  String title,
  @NotBlank(message = EMPTY_MESSAGE)
  String content
) {

    public static Post toEntity(Member member, PostCreateRequest postCreateRequest) {
        return Post.builder()
                   .title(postCreateRequest.title())
                   .content(postCreateRequest.content())
                   .region(member.getRegion())
                   .member(member)
                   .postImages(new ArrayList<>())
                   .build();
    }
}
