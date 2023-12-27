package matgo.restaurant.dto.response;

import matgo.restaurant.domain.entity.RestaurantSearch;

public record RestaurantSliceResponse(
  String id,
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

    public static RestaurantSliceResponse from(RestaurantSearch restaurantSearch) {
        return new RestaurantSliceResponse(
          restaurantSearch.getId(),
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
