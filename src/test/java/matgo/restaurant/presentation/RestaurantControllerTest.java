package matgo.restaurant.presentation;

import static matgo.restaurant.presentation.RestaurantDocument.approveRestaurantDocument;
import static matgo.restaurant.presentation.RestaurantDocument.getRestaurantDetailDocument;
import static matgo.restaurant.presentation.RestaurantDocument.getRestaurantsByAddressDocument;
import static matgo.restaurant.presentation.RestaurantDocument.getRestaurantsByRegionDocument;
import static matgo.restaurant.presentation.RestaurantDocument.getRestaurantsDocument;
import static matgo.restaurant.presentation.RestaurantDocument.requestNewRestaurantDocument;
import static matgo.restaurant.presentation.RestaurantDocument.searchRestaurantsDocument;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import matgo.common.BaseControllerTest;
import matgo.restaurant.domain.entity.Restaurant;
import matgo.restaurant.domain.entity.RestaurantSearch;
import matgo.restaurant.dto.request.CustomPageRequest;
import matgo.restaurant.dto.request.RestaurantRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;

class RestaurantControllerTest extends BaseControllerTest {

    CustomPageRequest customPageRequest = new CustomPageRequest(0, 10, Optional.of(Direction.DESC),
      Optional.of("rating"));
    private List<RestaurantSearch> mockSearchResults;

    @BeforeEach
    void setUp() {
        mockSearchResults = List.of(
          new RestaurantSearch("1", "a", "address", "효자동3가", "phoneNumber", 1.0, 1.0, "description", 1.0, 0),
          new RestaurantSearch("2", "b", "address", "효자동3가", "phoneNumber", 1.0, 1.0, "description", 2.0, 0),
          new RestaurantSearch("3", "c", "address", "효자동3가", "phoneNumber", 1.0, 1.0, "description", 3.0, 0),
          new RestaurantSearch("4", "d", "address", "효자동3가", "phoneNumber", 1.0, 1.0, "description", 4.0, 0),
          new RestaurantSearch("5", "e", "address", "효자동3가", "phoneNumber", 1.0, 1.0, "description", 5.0, 0),
          new RestaurantSearch("6", "f", "address", "효자동3가", "phoneNumber", 1.0, 1.0, "description", 4.0, 0),
          new RestaurantSearch("7", "g", "address", "효자동3가", "phoneNumber", 1.0, 1.0, "description", 3.0, 0),
          new RestaurantSearch("8", "h", "address", "효자동3가", "phoneNumber", 1.0, 1.0, "description", 2.0, 0),
          new RestaurantSearch("9", "i", "address", "효자동3가", "phoneNumber", 1.0, 1.0, "description", 1.0, 0),
          new RestaurantSearch("10", "j", "address", "효자동3가", "phoneNumber", 1.0, 1.0, "description", 1.0, 0)
        );
    }

    @Test
    @DisplayName("[성공]전체 식당 목록 조회")
    void getRestaurants_success() {
        // given
        Page<RestaurantSearch> page = new PageImpl<>(mockSearchResults);
        doReturn(page).when(restaurantSearchRepository).findAll(any(Pageable.class));

        // when
        Response response = customGivenWithDocs(getRestaurantsDocument())
          .accept(ContentType.JSON)
          .queryParam("page", customPageRequest.page())
          .queryParam("size", customPageRequest.size())
          .queryParam("direction", customPageRequest.direction().get())
          .queryParam("sortBy", customPageRequest.sortBy().get())
          .get("/api/restaurants");

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(200);
            softly.assertThat(response.body().jsonPath().getList("restaurants").size()).isEqualTo(10);
        });
    }

    @Test
    @DisplayName("[성공]주소로 식당 목록 조회")
    void getRestaurantsByAddress_success() {
        // given
        String keyword = "효자동3가";
        Page<RestaurantSearch> page = new PageImpl<>(mockSearchResults);
        doReturn(page).when(restaurantSearchRepository).findByAddressExactMatch(anyString(), any(Pageable.class));

        // when
        Response response = customGivenWithDocs(getRestaurantsByAddressDocument())
          .accept(ContentType.JSON)
          .queryParam("keyword", keyword)
          .queryParam("page", customPageRequest.page())
          .queryParam("size", customPageRequest.size())
          .queryParam("direction", customPageRequest.direction().get())
          .queryParam("sortBy", customPageRequest.sortBy().get())
          .get("/api/restaurants/address");

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(200);
            softly.assertThat(response.body().jsonPath().getList("restaurants").size()).isEqualTo(10);
        });
    }

    @Test
    @DisplayName("[성공]주변 식당 목록 조회")
    void getRestaurantsByRegion_success() {
        // given
        Page<RestaurantSearch> page = new PageImpl<>(mockSearchResults);
        doReturn(page).when(restaurantSearchRepository).findByAddressExactMatch(anyString(), any(Pageable.class));

        // when
        Response response = customGivenWithDocs(getRestaurantsByRegionDocument())
          .accept(ContentType.JSON)
          .header("Authorization", "Bearer " + accessToken)
          .queryParam("page", customPageRequest.page())
          .queryParam("size", customPageRequest.size())
          .queryParam("direction", customPageRequest.direction().get())
          .queryParam("sortBy", customPageRequest.sortBy().get())
          .get("/api/restaurants/nearby");

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(200);
            softly.assertThat(response.body().jsonPath().getList("restaurants").size()).isEqualTo(10);
        });
    }

    @Test
    @DisplayName("[성공]식당 상세 조회")
    void getRestaurantDetail_success() {
        // given
        Restaurant restaurant1 = Restaurant.builder()
                                           .name("test1")
                                           .externalId("1")
                                           .address("test1")
                                           .roadAddress("test1")
                                           .phoneNumber("test1")
                                           .lat(1.0)
                                           .lon(1.0)
                                           .description("test1")
                                           .approvedAt(LocalDateTime.now())
                                           .rating(0.0)
                                           .reviewCount(0)
                                           .build();

        restaurantRepository.save(restaurant1);
        Long restaurantId = 1L;

        // when
        Response response = customGivenWithDocs(getRestaurantDetailDocument())
          .accept(ContentType.JSON)
          .pathParam("restaurantId", restaurantId)
          .get("/api/restaurants/detail/{restaurantId}");

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(200);
            softly.assertThat(response.body().jsonPath().getLong("id")).isEqualTo(restaurantId);
            softly.assertThat(response.body().jsonPath().getString("name")).isEqualTo("test1");
        });
    }

    @Test
    @DisplayName("[성공]이름으로 식당 검색")
    void getSearchedRestaurantByName_success() {
        // given
        List<RestaurantSearch> mockSearchResults = List.of(
          new RestaurantSearch("1", "name", "address", "roadAddress", "phoneNumber", 1.0, 1.0, "description", 0.0, 0)
        );
        doReturn(mockSearchResults).when(restaurantSearchRepository).findByName(anyString());
        String name = "name";

        // when
        Response response = customGivenWithDocs(searchRestaurantsDocument())
          .accept(ContentType.JSON)
          .queryParam("name", name)
          .get("/api/restaurants/search");

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(200);
            softly.assertThat(response.body().jsonPath().getList("restaurants").size()).isEqualTo(1);
            softly.assertThat(response.body().jsonPath().getString("restaurants[0].name")).isEqualTo(name);
        });
    }


    @Test
    @DisplayName("[성공]새로운 식당 등록 요청")
    void requestNewRestaurant_success() {
        // given
        RestaurantRequest restaurantRequest = new RestaurantRequest("test1", "test1", "test1", "test1", 1.0, 1.0,
          "test1");

        // when
        Response response = customGivenWithDocs(requestNewRestaurantDocument())
          .accept(ContentType.JSON)
          .contentType(ContentType.JSON)
          .header("Authorization", "Bearer " + accessToken)
          .body(restaurantRequest)
          .post("/api/restaurants/new");

        System.out.println("response = " + response.asString());

        // then
        assertSoftly(softly -> softly.assertThat(response.statusCode()).isEqualTo(204));
    }

    @Test
    @DisplayName("[성공]식당 등록 승인 - 관리자")
    void approveRestaurant_success() {
        // given
        Restaurant restaurant1 = Restaurant.builder()
                                           .name("test1")
                                           .externalId("1")
                                           .address("test1")
                                           .roadAddress("test1")
                                           .phoneNumber("test1")
                                           .lat(1.0)
                                           .lon(1.0)
                                           .description("test1")
                                           .approvedAt(null)
                                           .rating(0.0)
                                           .reviewCount(0)
                                           .build();

        restaurantRepository.save(restaurant1);
        Long restaurantId = 1L;

        // when
        Response response = customGivenWithDocs(approveRestaurantDocument())
          .accept(ContentType.JSON)
          .header("Authorization", "Bearer " + adminAccessToken)
          .pathParam("restaurantId", restaurantId)
          .put("/api/restaurants/approve/{restaurantId}");

        // then
        assertSoftly(softly -> softly.assertThat(response.statusCode()).isEqualTo(204));
    }
}