package matgo.restaurant.presentation;

import lombok.RequiredArgsConstructor;
import matgo.auth.security.OnlyUser;
import matgo.restaurant.application.RestaurantService;
import matgo.restaurant.dto.response.RestaurantsSliceResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    public RestaurantsSliceResponse getRestaurants(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
        return restaurantService.getRestaurants(PageRequest.of(page, size));
    }

    @GetMapping("/address")
    public RestaurantsSliceResponse getRestaurantsByAddress(
      @RequestParam String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
    ) {
        return restaurantService.getRestaurantsByAddress(keyword, PageRequest.of(page, size));
    }

    @GetMapping("/nearby")
    @OnlyUser
    public RestaurantsSliceResponse getRestaurantsByRegion(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
    ) {
        return restaurantService.getRestaurantsByRegion(Long.parseLong(userDetails.getUsername()),
          PageRequest.of(page, size));
    }

}
