package matgo.member.dto.request;

import static matgo.global.util.DtoValidator.NAMING_FORMAT;
import static matgo.global.util.DtoValidator.NICKNAME_MESSAGE;

import jakarta.validation.constraints.Pattern;

public record MemberUpdateRequest(
  @Pattern(regexp = NAMING_FORMAT, message = NICKNAME_MESSAGE)
  String nickname,

  String region
) {

}
