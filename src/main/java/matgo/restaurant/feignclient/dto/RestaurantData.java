package matgo.restaurant.feignclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RestaurantData(
  @JsonProperty("식당명") String name,
  @JsonProperty("도로명주소") String roadAddress,
  @JsonProperty("지번주소") String address,
  @JsonProperty("식당대표전화번호") String phoneNumber,
  @JsonProperty("식당위도") Double lat,
  @JsonProperty("식당경도") Double lon,
  @JsonProperty("음식점소개내용") String description
) {

}
