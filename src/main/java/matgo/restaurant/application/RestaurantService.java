package matgo.restaurant.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matgo.restaurant.domain.entity.Restaurant;
import matgo.restaurant.domain.repository.RestaurantRepository;
import matgo.restaurant.feignclient.JeonjuRestaurantClient;
import matgo.restaurant.feignclient.dto.RestaurantData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {

    private static final int MAX_COUNT = 4000;
    
    private final JeonjuRestaurantClient jeonjuRestaurantClient;
    private final RestaurantRepository restaurantRepository;

    @Value("${external.jeonju-restaurant.key}")
    private String key;

    @Transactional
    // 한달에 한번씩 실행
    @Scheduled(cron = "0 0 0 1 * *")
    public void fetchAndSaveRestaurants() {
        List<RestaurantData> restaurantDataList = jeonjuRestaurantClient.getRestaurants(1, MAX_COUNT, key);
        List<Restaurant> restaurants = restaurantDataList.stream()
                                                         .map(Restaurant::from)
                                                         .toList();

        restaurantRepository.saveAll(restaurants);
    }

}
