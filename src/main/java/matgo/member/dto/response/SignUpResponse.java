package matgo.member.dto.response;

public record SignUpResponse(
  Long id
) {

    public static SignUpResponse from(Long id) {
        return new SignUpResponse(id);
    }
}
