package matgo.restaurant.application;

import static matgo.global.exception.ErrorCode.ELREADY_APPROVED_RESTAURANT;
import static matgo.global.exception.ErrorCode.ELREADY_EXISTED_RESTAURANT;
import static matgo.global.exception.ErrorCode.NOT_FOUND_MEMBER;
import static matgo.global.exception.ErrorCode.NOT_FOUND_RESTAURANT;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matgo.member.domain.entity.Member;
import matgo.member.domain.repository.MemberRepository;
import matgo.member.exception.MemberException;
import matgo.restaurant.domain.entity.Restaurant;
import matgo.restaurant.domain.entity.RestaurantSearch;
import matgo.restaurant.domain.repository.RestaurantQueryRepository;
import matgo.restaurant.domain.repository.RestaurantRepository;
import matgo.restaurant.domain.repository.RestaurantSearchRepository;
import matgo.restaurant.domain.repository.RestaurantSearchRepositoryImpl;
import matgo.restaurant.dto.request.RestaurantRequest;
import matgo.restaurant.dto.response.RestaurantDetailResponse;
import matgo.restaurant.dto.response.RestaurantSearchResponse;
import matgo.restaurant.dto.response.RestaurantSliceResponse;
import matgo.restaurant.dto.response.RestaurantsSliceResponse;
import matgo.restaurant.exception.RestaurantException;
import matgo.restaurant.feignclient.JeonjuRestaurantClient;
import matgo.restaurant.feignclient.dto.RestaurantData;
import matgo.restaurant.feignclient.dto.RestaurantDataResponse;
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
    private final RestaurantQueryRepository restaurantQueryRepository;
    private final RestaurantSearchRepository restaurantSearchRepository;
    private final RestaurantSearchRepositoryImpl restaurantSearchRepositoryImpl;

    @Value("${external.jeonju-restaurant.key}")
    private String key;

    @Transactional
    // 한달에 한번씩 실행
//    @Scheduled(cron = "0 0 0 1 * *")
    @Scheduled(cron = "0 20 * * * *")
    public void fetchAndSaveRestaurants() {
        int page = 1;
        int perPage = 100;

        RestaurantDataResponse response;
        do {
            response = fetchRestaurantsFromAPI(page, perPage);
            List<Restaurant> restaurants = convertToRestaurants(response.data());
            Map<String, Restaurant> existingRestaurantsMap = getExistingRestaurantsMap(restaurants);
            List<Restaurant> newRestaurants = getNewRestaurants(restaurants, existingRestaurantsMap);
            saveRestaurants(newRestaurants);
            page++;
        } while (response.page() * response.perPage() < response.totalCount());

        log.info("fetch and save restaurants success");
        indexingToES();
    }

    private RestaurantDataResponse fetchRestaurantsFromAPI(int page, int perPage) {
        return jeonjuRestaurantClient.getRestaurants(page, perPage, key);
    }

    private List<Restaurant> convertToRestaurants(List<RestaurantData> restaurantDataList) {
        return restaurantDataList.stream()
                                 .map(Restaurant::fromRestaurantData)
                                 .toList();
    }

    private Map<String, Restaurant> getExistingRestaurantsMap(List<Restaurant> restaurants) {
        List<String> restaurantExternalIds = restaurants.stream()
                                                        .map(Restaurant::getExternalId)
                                                        .toList();
        List<Restaurant> existingRestaurants = restaurantRepository.findByExternalIdIn(restaurantExternalIds);
        return existingRestaurants.stream()
                                  .collect(Collectors.toMap(Restaurant::getExternalId, restaurant -> restaurant));
    }

    private List<Restaurant> getNewRestaurants(List<Restaurant> restaurants,
      Map<String, Restaurant> existingRestaurantsMap) {
        List<Restaurant> newRestaurants = new ArrayList<>();
        for (Restaurant restaurant : restaurants) {
            String externalId = restaurant.getExternalId();
            Restaurant existingRestaurant = existingRestaurantsMap.get(externalId);
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
        List<Restaurant> restaurants = restaurantRepository.findByModifiedAtAfterAndApprovedAtIsNotNull(oneHourAgo);
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
                                                    .filter(restaurants -> restaurants.getApprovedAt() != null)
                                                    .orElseThrow(() -> new RestaurantException(NOT_FOUND_RESTAURANT));

        return RestaurantDetailResponse.from(restaurant);
    }

    @Transactional(readOnly = true)
    public RestaurantSearchResponse getSearchedRestaurantByName(String name) {
        List<RestaurantDetailResponse> restaurants = restaurantSearchRepository.findByName(name)
                                                                               .stream()
                                                                               .map(RestaurantDetailResponse::from)
                                                                               .toList();

        return new RestaurantSearchResponse(restaurants);
    }

    @Transactional
    public void requestNewRestaurant(RestaurantRequest restaurantRequest) {
        checkDuplicate(restaurantRequest.roadAddress(), restaurantRequest.address());

        restaurantRepository.save(Restaurant.fromRestaurantRequest(restaurantRequest));
    }

    private void checkDuplicate(String roadAddress, String address) {
        if (restaurantQueryRepository.existsByDuplicateField(roadAddress, address)) {
            throw new RestaurantException(ELREADY_EXISTED_RESTAURANT);
        }
    }

    @Transactional
    public void approveRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                                                    .orElseThrow(() -> new RestaurantException(NOT_FOUND_RESTAURANT));
        if (restaurant.getApprovedAt() != null) {
            throw new RestaurantException(ELREADY_APPROVED_RESTAURANT);
        }

        restaurant.approve();

        restaurantSearchRepository.save(RestaurantSearch.from(restaurant));
    }
}
