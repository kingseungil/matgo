package matgo.review.dto.request;

import static matgo.global.util.DtoValidator.EMPTY_MESSAGE;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import matgo.member.domain.entity.Member;
import matgo.restaurant.domain.entity.Restaurant;
import matgo.review.domain.entity.Review;

public record ReviewCreateRequest(

  @NotBlank(message = EMPTY_MESSAGE)
  String content,
  @NotNull(message = EMPTY_MESSAGE)
  int rating,
  @NotNull(message = EMPTY_MESSAGE)
  boolean revisit
) {

    public static Review toEntity(Member member, Restaurant restaurant, ReviewCreateRequest request, String imageUrl) {
        return Review.builder()
                     .member(member)
                     .restaurant(restaurant)
                     .content(request.content())
                     .rating(request.rating())
                     .imageUrl(imageUrl)
                     .revisit(request.revisit())
                     .build();
    }
}
