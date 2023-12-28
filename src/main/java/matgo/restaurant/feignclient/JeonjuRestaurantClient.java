package matgo.restaurant.feignclient;

import matgo.restaurant.feignclient.config.RestaurantFeignClientConfig;
import matgo.restaurant.feignclient.dto.RestaurantDataResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "jeonju-restaurant", url = "${external.jeonju-restaurant.url}", configuration = RestaurantFeignClientConfig.class)
public interface JeonjuRestaurantClient {

    @GetMapping
    RestaurantDataResponse getRestaurants(@RequestParam("page") int page, @RequestParam("perPage") int perPage,
      @RequestParam("serviceKey") String key);

}
