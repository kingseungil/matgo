package matgo.restaurant.presentation;

import static matgo.restaurant.presentation.RestaurantDocument.getRestaurantDetailDocument;
import static matgo.restaurant.presentation.RestaurantDocument.getRestaurantsByAddressDocument;
import static matgo.restaurant.presentation.RestaurantDocument.getRestaurantsByRegionDocument;
import static matgo.restaurant.presentation.RestaurantDocument.getRestaurantsDocument;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import matgo.common.BaseControllerTest;
import matgo.restaurant.domain.entity.Restaurant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RestaurantControllerTest extends BaseControllerTest {


    @Test
    @DisplayName("[성공]전체 식당 목록 조회")
    void getRestaurants_success() {
        // given,when
        Response response = customGivenWithDocs(getRestaurantsDocument())
          .accept(ContentType.JSON)
          .queryParam("page", 0)
          .queryParam("size", 10)
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

        // when
        Response response = customGivenWithDocs(getRestaurantsByAddressDocument())
          .accept(ContentType.JSON)
          .queryParam("keyword", keyword)
          .queryParam("page", 0)
          .queryParam("size", 10)
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
        // when
        Response response = customGivenWithDocs(getRestaurantsByRegionDocument())
          .accept(ContentType.JSON)
          .header("Authorization", "Bearer " + accessToken)
          .queryParam("page", 0)
          .queryParam("size", 10)
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
        Restaurant restaurant1 = new Restaurant("test1", "test1", "test1", "test1", 1.0, 1.0, "test1");
        Restaurant restaurant2 = new Restaurant("test2", "test2", "test2", "test2", 1.0, 1.0, "test2");

        restaurantRepository.save(restaurant1);
        restaurantRepository.save(restaurant2);
        Long restaurantId = 2L;

        // when
        Response response = customGivenWithDocs(getRestaurantDetailDocument())
          .accept(ContentType.JSON)
          .pathParam("restaurantId", restaurantId)
          .get("/api/restaurants/detail/{restaurantId}");

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(200);
            softly.assertThat(response.body().jsonPath().getLong("id")).isEqualTo(restaurantId);
            softly.assertThat(response.body().jsonPath().getString("name")).isEqualTo("test2");
        });

    }
}