package matgo.member.dto.request;

import static matgo.global.util.DtoValidator.EMPTY_MESSAGE;
import static matgo.global.util.DtoValidator.PW_FORMAT;
import static matgo.global.util.DtoValidator.PW_MESSAGE;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(
  // 현재 비밀번호
  @NotBlank(message = EMPTY_MESSAGE)
  @Pattern(regexp = PW_FORMAT, message = PW_MESSAGE)
  String currentPassword,

  // 바꿀 비밀번호
  @NotBlank(message = EMPTY_MESSAGE)
  @Pattern(regexp = PW_FORMAT, message = PW_MESSAGE)
  String newPassword
) {

}
