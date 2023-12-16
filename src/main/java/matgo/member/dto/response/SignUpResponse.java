package matgo.member.dto.response;

public record SignUpResponse(
  Long id,
  String email
) {

    public static SignUpResponse from(Long id, String email) {
        return new SignUpResponse(id, email);
    }
}
