package matgo.restaurant.application;

import static matgo.global.exception.ErrorCode.NOT_FOUND_RESTAURANT;
import static matgo.global.exception.ErrorCode.UPDATABLE_RESTAURANT_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import matgo.common.BaseServiceTest;
import matgo.member.domain.entity.Member;
import matgo.member.domain.entity.Region;
import matgo.restaurant.domain.entity.Restaurant;
import matgo.restaurant.domain.entity.RestaurantSearch;
import matgo.restaurant.dto.response.RestaurantDetailResponse;
import matgo.restaurant.dto.response.RestaurantsSliceResponse;
import matgo.restaurant.exception.RestaurantException;
import matgo.restaurant.feignclient.dto.RestaurantData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

class RestaurantServiceTest extends BaseServiceTest {

    @InjectMocks
    private RestaurantService restaurantService;

    @Nested
    @DisplayName("fetchAndSaveRestaurants 메서드는")
    class FetchAndSaveRestaurants {

        private static final int MAX_COUNT = 4000;

        RestaurantData restaurantData1 = new RestaurantData("name", "roadAddress", "address", "phoneNumber", 1.0,
          1.0, "description");
        RestaurantData restaurantData2 = new RestaurantData("name", "roadAddress", "address", "phoneNumber", 1.0,
          1.0, "description");

        Restaurant restaurant1 = Restaurant.from(restaurantData1);
        Restaurant restaurant2 = Restaurant.from(restaurantData2);

        @Test
        @DisplayName("성공하면 외부 API에서 받아온 데이터를 DB에 save (새로운 데이터)")
        void fetchAndSaveRestaurants_success_new_data() {
            // given
            List<RestaurantData> restaurantDataList = List.of(restaurantData1, restaurantData2);
            doReturn(restaurantDataList).when(jeonjuRestaurantClient).getRestaurants(1, MAX_COUNT, null);
            doReturn(List.of()).when(restaurantRepository).findByNameInAndAddressIn(anyList(), anyList());

            // when
            restaurantService.fetchAndSaveRestaurants();

            // then
            verify(restaurantRepository, times(2)).save(ArgumentMatchers.any(Restaurant.class));
        }

        @Test
        @DisplayName("성공하면 외부 API에서 받아온 데이터를 DB에 update (기존 데이터)")
        void fetchAndSaveRestaurants_success_existing_data() {
            // given
            RestaurantData newRestaurantData = new RestaurantData("newName", "roadAddress", "address", "phoneNumber",
              1.0, 1.0, "description");

            List<RestaurantData> restaurantDataList = List.of(newRestaurantData);
            doReturn(restaurantDataList).when(jeonjuRestaurantClient).getRestaurants(1, MAX_COUNT, null);

            List<Restaurant> existingRestaurants = List.of(restaurant1, restaurant2);
            doReturn(existingRestaurants).when(restaurantRepository).findByNameInAndAddressIn(anyList(), anyList());

            ArgumentCaptor<Restaurant> restaurantCaptor = ArgumentCaptor.forClass(Restaurant.class);

            // when
            restaurantService.fetchAndSaveRestaurants();

            // then
            verify(restaurantRepository, times(1)).save(restaurantCaptor.capture());
            Restaurant updatedRestaurant = restaurantCaptor.getValue();
            assertThat(updatedRestaurant.getName()).isEqualTo(newRestaurantData.name());
        }
    }

    @Nested
    @DisplayName("indexingToES 메서드는")
    class IndexingToES {

        Restaurant restaurant1 = new Restaurant(1L, "test1", "test1", "test1", "test1", 1.0, 1.0, "test1");
        Restaurant restaurant2 = new Restaurant(2L, "test2", "test2", "test2", "test2", 1.0, 1.0, "test2");

        @Test
        @DisplayName("성공하면 1시간 이내에 수정된 식당만 elasticsearch에 저장")
        void indexingToES_success() {
            // given
            List<Restaurant> restaurants = Arrays.asList(restaurant1, restaurant2);

            doReturn(restaurants).when(restaurantRepository).findByModifiedAtAfter(any(LocalDateTime.class));

            // when
            restaurantService.indexingToES();

            // then
            verify(restaurantSearchRepositoryImpl, times(1)).bulkInsertOrUpdate(anyList());
        }

        @Test
        @DisplayName("1시간 이내에 수정된 식당이 없으면 RestaurantException 발생")
        void indexingToES_fail() {
            // given
            doReturn(List.of()).when(restaurantRepository).findByModifiedAtAfter(any(LocalDateTime.class));

            // when & then
            assertThatThrownBy(() -> restaurantService.indexingToES())
              .isInstanceOf(RestaurantException.class)
              .hasMessageContaining(UPDATABLE_RESTAURANT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("getRestaurant 메서드는")
    class GetRestaurant {

        RestaurantSearch restaurantSearch1 = new RestaurantSearch("1", "test1", "test1", "test1", "test1", 1.0, 1.0,
          "test1");
        RestaurantSearch restaurantSearch2 = new RestaurantSearch("2", "test2", "test2", "test2", "test2", 1.0, 1.0,
          "test2");

        @Test
        @DisplayName("elasticsearch에 저장된 식당을 반환 (마지막 페이지)")
        void getRestaurants_last_page() {
            // given
            PageRequest pageRequest = PageRequest.of(0, 2);
            List<RestaurantSearch> restaurantSearches = Arrays.asList(restaurantSearch1, restaurantSearch2);
            Slice<RestaurantSearch> slice = new PageImpl<>(restaurantSearches, pageRequest, restaurantSearches.size());

            doReturn(slice).when(restaurantSearchRepository).findAll(pageRequest);

            // when
            RestaurantsSliceResponse response = restaurantService.getRestaurants(pageRequest);

            // then
            assertSoftly(softly -> {
                softly.assertThat(response.restaurants().size()).isEqualTo(2);
                softly.assertThat(response.restaurants().get(0).name()).isEqualTo("test1");
                softly.assertThat(response.restaurants().get(1).name()).isEqualTo("test2");
                softly.assertThat(response.hasNext()).isFalse();
            });
        }

        @Test
        @DisplayName("elasticsearch에 저장된 식당을 반환 (마지막 페이지가 아님)")
        void getRestaurants_not_last_page() {
            // given
            PageRequest pageRequest = PageRequest.of(0, 1);
            List<RestaurantSearch> restaurantSearches = Arrays.asList(restaurantSearch1, restaurantSearch2);
            Slice<RestaurantSearch> slice = new PageImpl<>(restaurantSearches, pageRequest, restaurantSearches.size());

            doReturn(slice).when(restaurantSearchRepository).findAll(any(Pageable.class));

            // when
            RestaurantsSliceResponse response = restaurantService.getRestaurants(pageRequest);

            // then
            assertSoftly(softly -> {
                softly.assertThat(response.restaurants().size()).isEqualTo(2);
                softly.assertThat(response.restaurants().get(0).name()).isEqualTo("test1");
                softly.assertThat(response.hasNext()).isTrue();
            });
        }
    }

    @Nested
    @DisplayName("getRestaurantsByAddress 메서드는")
    class GetRestaurantsByAddress {

        RestaurantSearch restaurantSearch1 = new RestaurantSearch("1", "test1", "test1", "완산구 효자동3가", "test1", 1.0, 1.0,
          "test1");
        RestaurantSearch restaurantSearch2 = new RestaurantSearch("2", "test2", "test2", "완산구 효자동3가", "test2", 1.0, 1.0,
          "test2");

        @Test
        @DisplayName("addressKeyword가 es에 저장된 address에 포함되어 있는 경우")
        void getRestaurantsByAddress_matching_keyword() {
            // given
            PageRequest pageRequest = PageRequest.of(0, 2);
            List<RestaurantSearch> restaurantSearches = Arrays.asList(restaurantSearch1, restaurantSearch2);
            Slice<RestaurantSearch> slice = new PageImpl<>(restaurantSearches, pageRequest, restaurantSearches.size());

            doReturn(slice).when(restaurantSearchRepository)
                           .findByAddressExactMatch(any(String.class), any(Pageable.class));

            // when
            RestaurantsSliceResponse response = restaurantService.getRestaurantsByAddress("효자동", pageRequest);

            // then
            assertSoftly(softly -> {
                softly.assertThat(response.restaurants().size()).isEqualTo(2);
                softly.assertThat(response.restaurants().get(0).name()).isEqualTo("test1");
                softly.assertThat(response.hasNext()).isFalse();
            });
        }

        @Test
        @DisplayName("addressKeyword가 es에 저장된 address에 포함되어 있지 않은 경우")
        void getRestaurantsByAddress_not_matching_keyword() {
            // given
            PageRequest pageRequest = PageRequest.of(0, 1);
            List<RestaurantSearch> restaurantSearches = List.of();
            Slice<RestaurantSearch> slice = new PageImpl<>(restaurantSearches, pageRequest, restaurantSearches.size());

            doReturn(slice).when(restaurantSearchRepository)
                           .findByAddressExactMatch(any(String.class), any(Pageable.class));

            // when
            RestaurantsSliceResponse response = restaurantService.getRestaurantsByAddress("test", pageRequest);

            // then
            assertSoftly(softly -> {
                softly.assertThat(response.restaurants().isEmpty()).isTrue();
                softly.assertThat(response.hasNext()).isFalse();
            });
        }
    }

    @Nested
    @DisplayName("getRestaurantsByRegion 메서드는")
    class GetRestaurantsByRegion {

        RestaurantSearch restaurantSearch1 = new RestaurantSearch("1", "test1", "test1", "완산구 효자동3가", "test1", 1.0, 1.0,
          "test1");
        RestaurantSearch restaurantSearch2 = new RestaurantSearch("2", "test2", "test2", "완산구 효자동3가", "test2", 1.0, 1.0,
          "test2");

        Member member = Member.builder()
                              .id(1L)
                              .region(new Region("효자동3가"))
                              .build();

        @Test
        @DisplayName("userId로 조회한 region이 es에 저장된 address에 포함되어 있는 경우")
        void getRestaurantsByRegion_matching_keyword() {
            // given
            Long userId = 1L;
            PageRequest pageRequest = PageRequest.of(0, 2);
            List<RestaurantSearch> restaurantSearches = Arrays.asList(restaurantSearch1, restaurantSearch2);
            Slice<RestaurantSearch> slice = new PageImpl<>(restaurantSearches, pageRequest, restaurantSearches.size());

            doReturn(Optional.of(member)).when(memberRepository).findById(userId);
            doReturn(slice).when(restaurantSearchRepository)
                           .findByAddressExactMatch(any(String.class), any(Pageable.class));

            // when
            RestaurantsSliceResponse response = restaurantService.getRestaurantsByRegion(userId, pageRequest);

            // then
            assertSoftly(softly -> {
                softly.assertThat(response.restaurants().size()).isEqualTo(2);
                softly.assertThat(response.hasNext()).isFalse();
            });
        }

        @Test
        @DisplayName("userId로 region을 조회하여 addressKeyword로 es에 저장된 address에 포함되어 있지 않은 경우")
        void getRestaurantsByRegion_not_matching_keyword() {
            // given
            Long userId = 1L;
            PageRequest pageRequest = PageRequest.of(0, 1);
            List<RestaurantSearch> restaurantSearches = List.of();
            Slice<RestaurantSearch> slice = new PageImpl<>(restaurantSearches, pageRequest, restaurantSearches.size());

            doReturn(Optional.of(member)).when(memberRepository).findById(userId);
            doReturn(slice).when(restaurantSearchRepository)
                           .findByAddressExactMatch(any(String.class), any(Pageable.class));

            // when
            RestaurantsSliceResponse response = restaurantService.getRestaurantsByRegion(userId, pageRequest);

            // then
            assertSoftly(softly -> {
                softly.assertThat(response.restaurants().isEmpty()).isTrue();
                softly.assertThat(response.hasNext()).isFalse();
            });
        }
    }

    @Nested
    @DisplayName("getRestaurantDetail 메서드는")
    class GetRestaurantDetail {

        Restaurant restaurant = new Restaurant(1L, "test", "test", "test", "test", 1.0, 1.0, "test");

        @Test
        @DisplayName("식당 상세 정보를 반환")
        void getRestaurantDetail() {
            // given
            doReturn(Optional.of(restaurant)).when(restaurantRepository).findById(any(Long.class));

            // when
            RestaurantDetailResponse response = restaurantService.getRestaurantDetail(1L);

            // then
            assertSoftly(softly -> {
                softly.assertThat(response.name()).isEqualTo("test");
                softly.assertThat(response.address()).isEqualTo("test");
                softly.assertThat(response.phoneNumber()).isEqualTo("test");
                softly.assertThat(response.lat()).isEqualTo(1.0);
                softly.assertThat(response.lon()).isEqualTo(1.0);
                softly.assertThat(response.description()).isEqualTo("test");
            });
        }

        @Test
        @DisplayName("식당이 존재하지 않으면 RestaurantException 발생")
        void getRestaurantDetail_fail() {
            // given
            doReturn(Optional.empty()).when(restaurantRepository).findById(any(Long.class));

            // when & then
            assertThatThrownBy(() -> restaurantService.getRestaurantDetail(1L))
              .isInstanceOf(RestaurantException.class)
              .hasMessageContaining(NOT_FOUND_RESTAURANT.getMessage());
        }
    }
}