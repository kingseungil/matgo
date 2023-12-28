package matgo.restaurant.application;

import static matgo.global.exception.ErrorCode.NOT_FOUND_MEMBER;
import static matgo.global.exception.ErrorCode.NOT_FOUND_RESTAURANT;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matgo.member.domain.entity.Member;
import matgo.member.domain.repository.MemberRepository;
import matgo.member.exception.MemberException;
import matgo.restaurant.domain.entity.Restaurant;
import matgo.restaurant.domain.entity.RestaurantSearch;
import matgo.restaurant.domain.repository.RestaurantRepository;
import matgo.restaurant.domain.repository.RestaurantSearchRepository;
import matgo.restaurant.domain.repository.RestaurantSearchRepositoryImpl;
import matgo.restaurant.dto.response.RestaurantDetailResponse;
import matgo.restaurant.dto.response.RestaurantSliceResponse;
import matgo.restaurant.dto.response.RestaurantsSliceResponse;
import matgo.restaurant.exception.RestaurantException;
import matgo.restaurant.feignclient.JeonjuRestaurantClient;
import matgo.restaurant.feignclient.dto.RestaurantData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {

    private final JeonjuRestaurantClient jeonjuRestaurantClient;
    private final MemberRepository memberRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantSearchRepository restaurantSearchRepository;
    private final RestaurantSearchRepositoryImpl restaurantSearchRepositoryImpl;

    @Value("${external.jeonju-restaurant.key}")
    private String key;

    @Transactional
    // 한달에 한번씩 실행
    @Scheduled(cron = "0 0 0 1 * *")
    public void fetchAndSaveRestaurants() {
        int page = 1;
        while (true) {
            List<RestaurantData> restaurantDataList = fetchRestaurantsFromAPI(page);
            if (restaurantDataList.isEmpty()) {
                break;
            }
            List<Restaurant> restaurants = convertToRestaurants(restaurantDataList);
            Map<String, Restaurant> existingRestaurantsMap = getExistingRestaurantsMap(restaurants);
            List<Restaurant> newRestaurants = getNewRestaurants(restaurants, existingRestaurantsMap);

            saveRestaurants(newRestaurants);
            log.info("fetch and save restaurants success");
            indexingToES();

            page++;
        }
    }

    private List<RestaurantData> fetchRestaurantsFromAPI(int page) {
        return jeonjuRestaurantClient.getRestaurants(page, 1000, key);
    }

    private List<Restaurant> convertToRestaurants(List<RestaurantData> restaurantDataList) {
        return restaurantDataList.stream()
                                 .map(Restaurant::from)
                                 .toList();
    }

    private Map<String, Restaurant> getExistingRestaurantsMap(List<Restaurant> restaurants) {
        List<String> restaurantNames = restaurants.stream()
                                                  .map(Restaurant::getName)
                                                  .toList();
        List<String> restaurantAddresses = restaurants.stream()
                                                      .map(Restaurant::getAddress)
                                                      .toList();
        List<Restaurant> existingRestaurants = restaurantRepository.findByNameInAndAddressIn(restaurantNames,
          restaurantAddresses);

        Map<String, Restaurant> map = new HashMap<>();
        for (Restaurant existingRestaurant : existingRestaurants) {
            map.put(existingRestaurant.getName() + existingRestaurant.getAddress(), existingRestaurant);
        }
        return map;
    }

    private List<Restaurant> getNewRestaurants(List<Restaurant> restaurants,
      Map<String, Restaurant> existingRestaurantsMap) {
        List<Restaurant> newRestaurants = new ArrayList<>();
        for (Restaurant restaurant : restaurants) {
            String key = restaurant.getName() + restaurant.getAddress();
            Restaurant existingRestaurant = existingRestaurantsMap.get(key);
            if (existingRestaurant != null) {
                existingRestaurant.update(restaurant);
            } else {
                newRestaurants.add(restaurant);
            }
        }
        return newRestaurants;
    }

    private void saveRestaurants(List<Restaurant> newRestaurants) {
        restaurantRepository.saveAll(newRestaurants);
    }

    @Transactional
    // 한시간에 한번씩 실행
    @Scheduled(cron = "0 0 * * * *")
    public void indexingToES() {
        // 1시간 이내에 수정된 식당만 elasticsearch에 저장
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<Restaurant> restaurants = restaurantRepository.findByModifiedAtAfter(oneHourAgo);
        if (restaurants.isEmpty()) {
            log.info("No restaurants updated in the last hour");
        } else {
            List<RestaurantSearch> restaurantSearches = restaurants.stream()
                                                                   .map(RestaurantSearch::from)
                                                                   .toList();
            restaurantSearchRepositoryImpl.bulkInsertOrUpdate(restaurantSearches);
            log.info("elasticsearch indexing success");
        }

    }


    @Transactional(readOnly = true)
    public RestaurantsSliceResponse getRestaurants(Pageable pageable) {
        Slice<RestaurantSearch> slice = restaurantSearchRepository.findAll(pageable);
        List<RestaurantSliceResponse> restaurants = slice.map(RestaurantSliceResponse::from).toList();

        return new RestaurantsSliceResponse(restaurants, slice.hasNext());
    }

    @Transactional(readOnly = true)
    public RestaurantsSliceResponse getRestaurantsByAddress(String addressKeyword, Pageable pageable) {
        Slice<RestaurantSearch> slice = restaurantSearchRepository.findByAddressExactMatch(addressKeyword, pageable);
        List<RestaurantSliceResponse> restaurants = slice.map(RestaurantSliceResponse::from).toList();

        return new RestaurantsSliceResponse(restaurants, slice.hasNext());
    }

    @Transactional(readOnly = true)
    public RestaurantsSliceResponse getRestaurantsByRegion(Long userId, Pageable pageable) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));

        Slice<RestaurantSearch> slice = restaurantSearchRepository.findByAddressExactMatch(member.getRegion().getName(),
          pageable);
        List<RestaurantSliceResponse> restaurants = slice.map(RestaurantSliceResponse::from).toList();

        return new RestaurantsSliceResponse(restaurants, slice.hasNext());
    }

    @Transactional(readOnly = true)
    public RestaurantDetailResponse getRestaurantDetail(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                                                    .orElseThrow(() -> new RestaurantException(NOT_FOUND_RESTAURANT));

        return RestaurantDetailResponse.from(restaurant);
    }

    // 리뷰 작성 후 호출되는 메서드 (maybe)
    @Transactional
    public void updateRestaurantRatingAndReviewCount(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                                                    .orElseThrow(() -> new RestaurantException(NOT_FOUND_RESTAURANT));

        // TODO : 매장 평점, 리뷰 개수 업데이트 (db, es 실시간 동기화)

        // 별점과 리뷰 개수 계산 로직

        // db 업데이트

        // es 업데이트
    }
}
