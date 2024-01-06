package matgo.restaurant.dto.response;

import matgo.restaurant.domain.entity.Restaurant;

public record RestaurantResponse(
  Long id,
  String name,
  String roadAddress
) {

    public static RestaurantResponse from(Restaurant restaurant) {
        return new RestaurantResponse(
          restaurant.getId(),
          restaurant.getName(),
          restaurant.getRoadAddress()
        );
    }
}
