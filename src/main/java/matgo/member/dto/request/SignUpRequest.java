package matgo.member.dto.request;

import static matgo.global.util.DtoValidator.EMAIL_MESSAGE;
import static matgo.global.util.DtoValidator.EMPTY_MESSAGE;
import static matgo.global.util.DtoValidator.NAMING_FORMAT;
import static matgo.global.util.DtoValidator.NICKNAME_MESSAGE;
import static matgo.global.util.DtoValidator.PW_FORMAT;
import static matgo.global.util.DtoValidator.PW_MESSAGE;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignUpRequest(
  @NotBlank(message = EMPTY_MESSAGE)
  @Email(message = EMAIL_MESSAGE)
  String email,
  @NotBlank(message = EMPTY_MESSAGE)
  @Pattern(regexp = NAMING_FORMAT, message = NICKNAME_MESSAGE)
  String nickname,
  @NotBlank(message = EMPTY_MESSAGE)
  @Pattern(regexp = PW_FORMAT, message = PW_MESSAGE)
  String password,
  @NotBlank(message = EMPTY_MESSAGE)
  String region
) {

}
