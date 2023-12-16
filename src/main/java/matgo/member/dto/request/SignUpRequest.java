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
import org.springframework.web.multipart.MultipartFile;

public record SignUpRequest(
  @NotBlank(message = EMPTY_MESSAGE)
  @Email(message = EMAIL_MESSAGE)
  String email,
  @NotBlank(message = EMAIL_MESSAGE)
  @Pattern(regexp = NAMING_FORMAT, message = NICKNAME_MESSAGE)
  String nickname,
  @NotBlank(message = EMPTY_MESSAGE)
  @Pattern(regexp = PW_FORMAT, message = PW_MESSAGE)
  String password,
  MultipartFile profileImage,
  @NotBlank(message = EMPTY_MESSAGE)
  String region
) {

    public static SignUpRequest of(
      String email,
      String nickname,
      String password,
      MultipartFile profileImage
    ) {
        return new SignUpRequest(email, nickname, password, profileImage, null);
    }
}
