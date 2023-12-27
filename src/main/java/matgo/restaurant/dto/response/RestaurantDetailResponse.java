package matgo.restaurant.dto.response;

import matgo.restaurant.domain.entity.Restaurant;

public record RestaurantDetailResponse(
  Long id,
  String name,
  String roadAddress,
  String address,
  String phoneNumber,
  Double lat,
  Double lon,
  String description,
  Double rating,
  Integer reviewCount
) {

    public static RestaurantDetailResponse from(Restaurant restaurant) {
        return new RestaurantDetailResponse(
          restaurant.getId(),
          restaurant.getName(),
          restaurant.getRoadAddress(),
          restaurant.getAddress(),
          restaurant.getPhoneNumber(),
          restaurant.getLat(),
          restaurant.getLon(),
          restaurant.getDescription(),
          restaurant.getRating(),
          restaurant.getReviewCount()
        );
    }

}
