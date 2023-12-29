package matgo.review.dto.request;

public record ReviewCreateRequest(
  String content,
  int rating,
  boolean revisit
) {

}
