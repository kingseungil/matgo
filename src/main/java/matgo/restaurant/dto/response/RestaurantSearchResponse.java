package matgo.restaurant.dto.response;

import java.util.List;

public record RestaurantSearchResponse(
  List<RestaurantDetailResponse> restaurants
) {

}
