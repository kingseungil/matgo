package matgo.auth.dto.request;

import static matgo.global.util.DtoValidator.EMAIL_MESSAGE;
import static matgo.global.util.DtoValidator.EMPTY_MESSAGE;
import static matgo.global.util.DtoValidator.PW_FORMAT;
import static matgo.global.util.DtoValidator.PW_MESSAGE;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import matgo.member.domain.type.UserRole;

public record LoginRequest(

  @NotBlank(message = EMPTY_MESSAGE)
  @Email(message = EMAIL_MESSAGE)
  String email,

  @NotBlank(message = EMPTY_MESSAGE)
  @Pattern(regexp = PW_FORMAT, message = PW_MESSAGE)
  String password,

  @NotNull(message = EMPTY_MESSAGE)
  UserRole role
) {

}
