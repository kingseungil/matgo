package matgo.auth.dto.request;

import static matgo.global.util.DtoValidator.EMAIL_MESSAGE;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record SendTemporaryPasswordRequest(
  @Email(message = EMAIL_MESSAGE)
  @NotNull(message = EMAIL_MESSAGE)
  String email
) {

}
