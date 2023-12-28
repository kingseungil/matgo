package matgo.restaurant.application;

import static matgo.global.exception.ErrorCode.ELREADY_APPROVED_RESTAURANT;
import static matgo.global.exception.ErrorCode.ELREADY_EXISTED_RESTAURANT;
import static matgo.global.exception.ErrorCode.NOT_FOUND_RESTAURANT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
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
import matgo.restaurant.dto.request.RestaurantRequest;
import matgo.restaurant.dto.response.RestaurantDetailResponse;
import matgo.restaurant.dto.response.RestaurantsSliceResponse;
import matgo.restaurant.exception.RestaurantException;
import matgo.restaurant.feignclient.dto.RestaurantData;
import matgo.restaurant.feignclient.dto.RestaurantDataResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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


        RestaurantData restaurantData1 = new RestaurantData("1", "name", "roadAddress", "address", "phoneNumber", 1.0,
          1.0, "description");
        RestaurantData restaurantData2 = new RestaurantData("2", "name", "roadAddress", "address", "phoneNumber", 1.0,
          1.0, "description");
        int page = 1;
        int perPage = 100;

        @Test
        @DisplayName("성공하면 외부 API에서 받아온 데이터를 DB에 save (새로운 데이터)")
        void fetchAndSaveRestaurants_success_new_data() {
            // given
            List<RestaurantData> restaurantDataList = List.of(restaurantData1, restaurantData2);
            RestaurantDataResponse response = new RestaurantDataResponse(page, perPage, 2, 0, 2,
              restaurantDataList);

            doReturn(response).when(jeonjuRestaurantClient).getRestaurants(anyInt(), anyInt(), isNull());
            doReturn(List.of()).when(restaurantRepository).findByExternalIdIn(anyList());

            // when
            restaurantService.fetchAndSaveRestaurants();

            // then
            verify(jeonjuRestaurantClient, times(1)).getRestaurants(anyInt(), anyInt(), isNull());
            verify(restaurantRepository, times(1)).findByExternalIdIn(anyList());
            verify(restaurantRepository, times(1)).saveAll(anyList());
        }

        @Test
        @DisplayName("성공하면 외부 API에서 받아온 데이터를 DB에 update (기존 데이터)")
        void fetchAndSaveRestaurants_success_existing_data() {
            // given
            List<RestaurantData> restaurantDataList = List.of(restaurantData1, restaurantData2);
            RestaurantDataResponse response = new RestaurantDataResponse(page, perPage, 2, 0, 2,
              restaurantDataList);

            doReturn(response).when(jeonjuRestaurantClient).getRestaurants(anyInt(), anyInt(), isNull());

            // 모킹된 Restaurant 객체 생성
            Restaurant mockRestaurant1 = mock(Restaurant.class);
            Restaurant mockRestaurant2 = mock(Restaurant.class);
            doReturn("1").when(mockRestaurant1).getExternalId();
            doReturn("2").when(mockRestaurant2).getExternalId();

            List<Restaurant> existingRestaurants = List.of(mockRestaurant1, mockRestaurant2);
            doReturn(existingRestaurants).when(restaurantRepository).findByExternalIdIn(anyList());

            // when
            restaurantService.fetchAndSaveRestaurants();

            // then
            verify(mockRestaurant1, times(1)).update(any(Restaurant.class));
            verify(mockRestaurant2, times(1)).update(any(Restaurant.class));
        }
    }

    @Nested
    @DisplayName("indexingToES 메서드는")
    class IndexingToES {

        Restaurant restaurant1 = mock(Restaurant.class);
        Restaurant restaurant2 = mock(Restaurant.class);

        @Test
        @DisplayName("성공하면 1시간 이내에 수정된 식당만 elasticsearch에 저장")
        void indexingToES_success() {
            // given
            List<Restaurant> restaurants = Arrays.asList(restaurant1, restaurant2);

            doReturn(restaurants).when(restaurantRepository)
                                 .findByModifiedAtAfterAndApprovedAtIsNotNull(any(LocalDateTime.class));

            // when
            restaurantService.indexingToES();

            // then
            verify(restaurantSearchRepositoryImpl, times(1)).bulkInsertOrUpdate(anyList());
        }
    }

    @Nested
    @DisplayName("getRestaurant 메서드는")
    class GetRestaurant {

        RestaurantSearch restaurantSearch1 = new RestaurantSearch("1", "test1", "test1", "test1", "test1", 1.0, 1.0,
          "test1", 0.0, 0);
        RestaurantSearch restaurantSearch2 = new RestaurantSearch("2", "test2", "test2", "test2", "test2", 1.0, 1.0,
          "test2", 0.0, 0);

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
          "test1", 0.0, 0);
        RestaurantSearch restaurantSearch2 = new RestaurantSearch("2", "test2", "test2", "완산구 효자동3가", "test2", 1.0, 1.0,
          "test2", 0.0, 0);

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
          "test1", 0.0, 0);
        RestaurantSearch restaurantSearch2 = new RestaurantSearch("2", "test2", "test2", "완산구 효자동3가", "test2", 1.0, 1.0,
          "test2", 0.0, 0);

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

        Restaurant restaurant = mock(Restaurant.class);

        @Test
        @DisplayName("식당 상세 정보를 반환")
        void getRestaurantDetail() {
            // given
            doReturn(1L).when(restaurant).getId();
            doReturn(LocalDateTime.now()).when(restaurant).getApprovedAt();
            doReturn(Optional.of(restaurant)).when(restaurantRepository).findById(any(Long.class));

            // when
            RestaurantDetailResponse response = restaurantService.getRestaurantDetail(1L);

            // then
            assertThat(response.id()).isEqualTo(1L);
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

    @Nested
    @DisplayName("requestNewRestaurant 메서드는")
    class RequestNewRestaurant {

        RestaurantRequest restaurantRequest = new RestaurantRequest("name", "roadAddress", "address", "phoneNumber",
          1.0, 1.0, "description");

        @Test
        @DisplayName("성공하면 DB에 저장")
        void requestNewRestaurant() {
            // given
            doReturn(false).when(restaurantQueryRepository).existsByDuplicateField(any(RestaurantRequest.class));
            // when
            restaurantService.requestNewRestaurant(restaurantRequest);

            // then
            verify(restaurantRepository, times(1)).save(any(Restaurant.class));
        }

        @Test
        @DisplayName("주소가 중복되면 RestaurantException 발생")
        void requestNewRestaurant_fail() {
            // given
            doReturn(true).when(restaurantQueryRepository).existsByDuplicateField(any(RestaurantRequest.class));

            // when & then
            assertThatThrownBy(() -> restaurantService.requestNewRestaurant(restaurantRequest))
              .isInstanceOf(RestaurantException.class)
              .hasMessageContaining(ELREADY_EXISTED_RESTAURANT.getMessage());
        }
    }

    @Nested
    @DisplayName("approveRestaurant 메서드는")
    class ApproveRestaurant {

        Restaurant mockRestaurant = mock(Restaurant.class);

        @Test
        @DisplayName("성공하면 DB와 ES에 저장")
        void approveRestaurant() {
            // given
            doReturn(1L).when(mockRestaurant).getId();
            doReturn(null).when(mockRestaurant).getApprovedAt();
            doReturn(Optional.of(mockRestaurant)).when(restaurantRepository).findById(any(Long.class));

            // when
            restaurantService.approveRestaurant(1L);

            // then
            verify(mockRestaurant, times(1)).approve();
            verify(restaurantSearchRepository, times(1)).save(any(RestaurantSearch.class));
        }

        @Test
        @DisplayName("식당이 존재하지 않으면 RestaurantException 발생")
        void approveRestaurant_fail() {
            // given
            doReturn(Optional.empty()).when(restaurantRepository).findById(any(Long.class));

            // when & then
            assertThatThrownBy(() -> restaurantService.approveRestaurant(1L))
              .isInstanceOf(RestaurantException.class)
              .hasMessageContaining(NOT_FOUND_RESTAURANT.getMessage());
        }

        @Test
        @DisplayName("이미 승인된 식당이면 RestaurantException 발생")
        void approveRestaurant_fail_already_approved() {
            // given
            doReturn(Optional.of(mockRestaurant)).when(restaurantRepository).findById(any(Long.class));
            doReturn(LocalDateTime.now()).when(mockRestaurant).getApprovedAt();

            // when & then
            assertThatThrownBy(() -> restaurantService.approveRestaurant(1L))
              .isInstanceOf(RestaurantException.class)
              .hasMessageContaining(ELREADY_APPROVED_RESTAURANT.getMessage());
        }
    }
}