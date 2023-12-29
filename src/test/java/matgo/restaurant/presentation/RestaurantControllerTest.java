package matgo.restaurant.presentation;

import static matgo.restaurant.presentation.RestaurantDocument.approveRestaurantDocument;
import static matgo.restaurant.presentation.RestaurantDocument.getRestaurantDetailDocument;
import static matgo.restaurant.presentation.RestaurantDocument.getRestaurantsByAddressDocument;
import static matgo.restaurant.presentation.RestaurantDocument.getRestaurantsByRegionDocument;
import static matgo.restaurant.presentation.RestaurantDocument.getRestaurantsDocument;
import static matgo.restaurant.presentation.RestaurantDocument.requestNewRestaurantDocument;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import matgo.common.BaseControllerTest;
import matgo.restaurant.domain.entity.Restaurant;
import matgo.restaurant.dto.request.CustomPageRequest;
import matgo.restaurant.dto.request.RestaurantRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort.Direction;

class RestaurantControllerTest extends BaseControllerTest {

    CustomPageRequest customPageRequest = new CustomPageRequest(0, 10, Optional.of(Direction.DESC),
      Optional.of("rating"));

    @Test
    @DisplayName("[성공]전체 식당 목록 조회")
    void getRestaurants_success() {
        // given

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
            List<Double> ratings = response.body().jsonPath().getList("restaurants.rating");
            softly.assertThat(ratings).isSortedAccordingTo(Comparator.reverseOrder());
        });
    }

    @Test
    @DisplayName("[성공]주소로 식당 목록 조회")
    void getRestaurantsByAddress_success() {
        // given
        String keyword = "효자동3가";

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
            List<Double> ratings = response.body().jsonPath().getList("restaurants.rating");
            softly.assertThat(ratings).isSortedAccordingTo(Comparator.reverseOrder());
        });
    }

    @Test
    @DisplayName("[성공]주변 식당 목록 조회")
    void getRestaurantsByRegion_success() {
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
            List<Double> ratings = response.body().jsonPath().getList("restaurants.rating");
            softly.assertThat(ratings).isSortedAccordingTo(Comparator.reverseOrder());
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