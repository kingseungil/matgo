package matgo.restaurant.dto.response;

import matgo.restaurant.domain.entity.Restaurant;
import matgo.restaurant.domain.entity.RestaurantSearch;

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

    public static RestaurantDetailResponse from(RestaurantSearch restaurantSearch) {
        return new RestaurantDetailResponse(
          Long.parseLong(restaurantSearch.getId()),
          restaurantSearch.getName(),
          restaurantSearch.getRoadAddress(),
          restaurantSearch.getAddress(),
          restaurantSearch.getPhoneNumber(),
          restaurantSearch.getLat(),
          restaurantSearch.getLon(),
          restaurantSearch.getDescription(),
          restaurantSearch.getRating(),
          restaurantSearch.getReviewCount()
        );
    }

}
