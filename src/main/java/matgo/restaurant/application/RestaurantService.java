package matgo.restaurant.application;

import static matgo.global.exception.ErrorCode.UPDATABLE_RESTAURANT_NOT_FOUND;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matgo.restaurant.domain.entity.Restaurant;
import matgo.restaurant.domain.entity.RestaurantSearch;
import matgo.restaurant.domain.repository.RestaurantRepository;
import matgo.restaurant.domain.repository.RestaurantSearchRepositoryImpl;
import matgo.restaurant.exception.RestaurantException;
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
    private final RestaurantSearchRepositoryImpl restaurantSearchRepository;

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
        log.info("fetch and save restaurants success");
    }

    @Transactional
    @Scheduled(cron = "0 6 * * * *")
    public void indexingToES() {
        // 1시간 이내에 수정된 식당만 elasticsearch에 저장
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<Restaurant> restaurants = restaurantRepository.findByModifiedAtAfter(oneHourAgo);
        if (restaurants.isEmpty()) {
            throw new RestaurantException(UPDATABLE_RESTAURANT_NOT_FOUND);
        }

        List<RestaurantSearch> restaurantSearches = restaurants.stream()
                                                               .map(RestaurantSearch::from)
                                                               .toList();
        restaurantSearchRepository.bulkInsertOrUpdate(restaurantSearches);
        log.info("elasticsearch indexing success");
    }
}
