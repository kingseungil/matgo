package matgo.auth.dto.request;

public record EmailVerificationRequest(
  String email,
  String code
) {

}
