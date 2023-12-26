package matgo.restaurant.feignclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RestaurantDataResponse(
  @JsonProperty("page") int page,
  @JsonProperty("perPage") int perPage,
  @JsonProperty("totalCount") int totalCount,
  @JsonProperty("currentCount") int currentCount,
  @JsonProperty("matchCount") int matchCount,
  @JsonProperty("data") List<RestaurantData> data
) {

}
