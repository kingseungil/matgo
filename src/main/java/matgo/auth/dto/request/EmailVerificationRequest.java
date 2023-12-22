package matgo.auth.dto.request;

import static matgo.global.util.DtoValidator.EMAIL_MESSAGE;
import static matgo.global.util.DtoValidator.EMPTY_MESSAGE;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailVerificationRequest(

  @NotBlank(message = EMPTY_MESSAGE)
  @Email(message = EMAIL_MESSAGE)
  String email,

  @NotBlank(message = EMPTY_MESSAGE)
  String code
) {

}
