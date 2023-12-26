package matgo.restaurant.dto.response;

import java.util.List;

public record RestaurantsSliceResponse(
  List<RestaurantSliceResponse> restaurants,
  boolean hasNext
) {

}
