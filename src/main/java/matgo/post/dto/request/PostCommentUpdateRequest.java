package matgo.post.dto.request;

import static matgo.global.util.DtoValidator.EMPTY_MESSAGE;

import jakarta.validation.constraints.NotBlank;

public record PostCommentUpdateRequest(
  @NotBlank(message = EMPTY_MESSAGE)
  String content
) {
    
}
