package matgo.restaurant.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import matgo.auth.security.OnlyAdmin;
import matgo.auth.security.OnlyUser;
import matgo.restaurant.application.RestaurantService;
import matgo.restaurant.dto.request.PageRequest;
import matgo.restaurant.dto.request.RestaurantRequest;
import matgo.restaurant.dto.response.RestaurantDetailResponse;
import matgo.restaurant.dto.response.RestaurantsSliceResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
      @Valid PageRequest pageRequest
    ) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
          pageRequest.page(),
          pageRequest.size(),
          pageRequest.getSort()
        );
        return restaurantService.getRestaurants(pageable);
    }

    @GetMapping("/address")
    public RestaurantsSliceResponse getRestaurantsByAddress(
      @RequestParam String keyword,
      @Valid PageRequest pageRequest
    ) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
          pageRequest.page(),
          pageRequest.size(),
          pageRequest.getSort()
        );
        return restaurantService.getRestaurantsByAddress(keyword, pageable);
    }

    @GetMapping("/nearby")
    @OnlyUser
    public RestaurantsSliceResponse getRestaurantsByRegion(
      @AuthenticationPrincipal UserDetails userDetails,
      @Valid PageRequest pageRequest
    ) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
          pageRequest.page(),
          pageRequest.size(),
          pageRequest.getSort()
        );
        return restaurantService.getRestaurantsByRegion(Long.parseLong(userDetails.getUsername()), pageable);
    }

    @GetMapping("/detail/{restaurantId}")
    public RestaurantDetailResponse getRestaurantDetail(
      @PathVariable Long restaurantId
    ) {
        return restaurantService.getRestaurantDetail(restaurantId);
    }

    @PostMapping("/new")
    @OnlyUser
    public ResponseEntity<Void> requestNewRestaurant(
      @Valid @RequestBody RestaurantRequest restaurantRequest
    ) {
        restaurantService.requestNewRestaurant(restaurantRequest);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/approve/{restaurantId}")
    @OnlyAdmin
    public ResponseEntity<Void> approveRestaurant(
      @PathVariable Long restaurantId
    ) {
        restaurantService.approveRestaurant(restaurantId);
        return ResponseEntity.noContent().build();
    }
}
