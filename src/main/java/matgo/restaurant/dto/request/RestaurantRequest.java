package matgo.restaurant.dto.request;

import static matgo.global.util.DtoValidator.EMPTY_MESSAGE;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RestaurantRequest(
  @NotBlank(message = EMPTY_MESSAGE)
  String name,

  @NotBlank(message = EMPTY_MESSAGE)
  String roadAddress,

  @NotBlank(message = EMPTY_MESSAGE)
  String address,

  @NotBlank(message = EMPTY_MESSAGE)
  String phoneNumber,

  @NotNull(message = EMPTY_MESSAGE)
  Double lat,

  @NotNull(message = EMPTY_MESSAGE)
  Double lon,

  @NotBlank(message = EMPTY_MESSAGE)
  String description
) {

}
