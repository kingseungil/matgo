package matgo.review.presentation;

import static matgo.review.presentation.ReviewDocument.addReviewReactionDocument;
import static matgo.review.presentation.ReviewDocument.createReviewDocument;
import static matgo.review.presentation.ReviewDocument.deleteReviewDocument;
import static matgo.review.presentation.ReviewDocument.getMyReviewsDocument;
import static matgo.review.presentation.ReviewDocument.getReviewDetailDocument;
import static matgo.review.presentation.ReviewDocument.getReviewsDocument;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.Optional;
import matgo.common.BaseControllerTest;
import matgo.global.type.Reaction;
import matgo.global.type.S3Directory;
import matgo.restaurant.domain.entity.Restaurant;
import matgo.restaurant.dto.request.CustomPageRequest;
import matgo.review.domain.entity.Review;
import matgo.review.dto.response.MyReviewSliceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.multipart.MultipartFile;

class ReviewControllerTest extends BaseControllerTest {

    CustomPageRequest customPageRequest = new CustomPageRequest(0, 10, Optional.of(Direction.DESC),
      Optional.of("rating"));
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        restaurant = Restaurant.builder()
                               .id(1L)
                               .externalId("1")
                               .name("식당")
                               .roadAddress("서울시 강남구 테헤란로 427")
                               .address("서울시 강남구 테헤란로 427")
                               .phoneNumber("010-1234-5678")
                               .lat(37.123456)
                               .lon(127.123456)
                               .description("맛집입니다.")
                               .rating(0.0)
                               .reviewCount(0)
                               .approvedAt(LocalDateTime.now())
                               .build();

        restaurantRepository.save(restaurant);


    }

    @Test
    @DisplayName("[성공]리뷰 작성")
    void createReview_success() {
        // given
        doNothing().when(restaurantSearchRepositoryImpl).updateRatingAndReviewCount(anyString(), anyDouble(), anyInt());
        Long restaurantId = 1L;
        MultiPartSpecBuilder request = new MultiPartSpecBuilder(reviewCreateRequest);
        request.charset("UTF-8");
        request.controlName("reviewCreateRequest");
        request.mimeType("application/json");
        doReturn("mocked_url").when(s3Service)
                              .uploadAndGetImageURL(any(MultipartFile.class), eq(S3Directory.REVIEW));

        // when
        Response response = customGivenWithDocs(createReviewDocument())
          .contentType("multipart/form-data;charset=UTF-8")
          .pathParam("restaurantId", restaurantId)
          .header("Authorization", "Bearer " + accessToken)
          .multiPart(request.build())
          .multiPart("reviewImage", image, "image/jpeg")
          .accept(ContentType.JSON)
          .post("/api/reviews/new/{restaurantId}", restaurantId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(201);
            softly.assertThat(response.header("Location")).isNotNull();
        });
    }

    @Test
    @DisplayName("[성공]리뷰 좋아요/싫어요 기능")
    void addReviewReaction_success() {
        // given
        Long reviewId = 1L;
        Review review = Review.builder()
                              .id(reviewId)
                              .content("리뷰")
                              .rating(5)
                              .imageUrl("mocked_url")
                              .revisit(true)
                              .likeCount(0)
                              .dislikeCount(0)
                              .member(member)
                              .restaurant(restaurant)
                              .build();
        reviewRepository.save(review);
        Reaction reactionType = Reaction.LIKE;

        // when
        Response response = customGivenWithDocs(addReviewReactionDocument())
          .contentType(ContentType.JSON)
          .pathParam("reviewId", reviewId)
          .queryParam("reactionType", reactionType)
          .header("Authorization", "Bearer " + accessToken)
          .post("/api/reviews/{reviewId}/reactions", reviewId);

        // then
        assertThat(response.statusCode()).isEqualTo(204);
    }

    @Test
    @DisplayName("[성공]리뷰 조회(페이징)")
    void getReviews_success() {
        // given
        Long restaurantId = 1L;
        Review review = Review.builder()
                              .content("리뷰")
                              .rating(5)
                              .imageUrl("mocked_url")
                              .revisit(true)
                              .likeCount(0)
                              .dislikeCount(0)
                              .member(member)
                              .restaurant(restaurant)
                              .build();
        reviewRepository.save(review);

        // when
        Response response = customGivenWithDocs(getReviewsDocument())
          .contentType(ContentType.JSON)
          .pathParam("restaurantId", restaurantId)
          .queryParam("page", customPageRequest.page())
          .queryParam("size", customPageRequest.size())
          .queryParam("direction", customPageRequest.direction().get())
          .queryParam("sortBy", customPageRequest.sortBy().get())
          .get("/api/reviews/{restaurantId}", restaurantId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(200);
            softly.assertThat(response.body().jsonPath().getList("reviews").size()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("[성공]리뷰 상세 보기")
    void getReview_success() {
        // given
        Long reviewId = 1L;
        Review review = Review.builder()
                              .id(reviewId)
                              .content("리뷰")
                              .rating(5)
                              .imageUrl("mocked_url")
                              .revisit(true)
                              .likeCount(0)
                              .dislikeCount(0)
                              .member(member)
                              .restaurant(restaurant)
                              .build();
        reviewRepository.save(review);

        // when
        Response response = customGivenWithDocs(getReviewDetailDocument())
          .contentType(ContentType.JSON)
          .pathParam("reviewId", reviewId)
          .get("/api/reviews/detail/{reviewId}", reviewId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(200);
            softly.assertThat(response.body().jsonPath().getLong("review.id")).isEqualTo(reviewId);
        });
    }

    @Test
    @DisplayName("[성공]리뷰 삭제")
    void deleteReview_success() {
        // given
        doNothing().when(restaurantSearchRepositoryImpl).updateRatingAndReviewCount(anyString(), anyDouble(), anyInt());
        Long restaurantId = 1L;
        Long reviewId = 1L;
        Review review = Review.builder()
                              .id(reviewId)
                              .content("리뷰")
                              .rating(5)
                              .imageUrl("mocked_url")
                              .revisit(true)
                              .likeCount(0)
                              .dislikeCount(0)
                              .member(member)
                              .restaurant(restaurant)
                              .build();
        reviewRepository.save(review);

        // when
        Response response = customGivenWithDocs(deleteReviewDocument())
          .contentType(ContentType.JSON)
          .pathParam("restaurantId", restaurantId)
          .pathParam("reviewId", reviewId)
          .header("Authorization", "Bearer " + accessToken)
          .delete("/api/reviews/{restaurantId}/{reviewId}", restaurantId, reviewId);

        // then
        assertThat(response.statusCode()).isEqualTo(204);
    }

    @Test
    @DisplayName("[성공]내가 작성한 리뷰 조회(페이징)")
    void getMyReviews_success() {
        // given
        Long reviewId = 1L;
        Review review = Review.builder()
                              .id(reviewId)
                              .content("리뷰")
                              .rating(5)
                              .imageUrl("mocked_url")
                              .revisit(true)
                              .likeCount(0)
                              .dislikeCount(0)
                              .member(member)
                              .restaurant(restaurant)
                              .build();
        reviewRepository.save(review);
        // when
        Response response = customGivenWithDocs(getMyReviewsDocument())
          .contentType(ContentType.JSON)
          .header("Authorization", "Bearer " + accessToken)
          .queryParam("page", customPageRequest.page())
          .queryParam("size", customPageRequest.size())
          .queryParam("direction", customPageRequest.direction().get())
          .queryParam("sortBy", customPageRequest.sortBy().get())
          .get("/api/reviews/my/writable-reviews");

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(200);
            MyReviewSliceResponse myReviewSliceResponse = response.as(MyReviewSliceResponse.class);
            softly.assertThat(myReviewSliceResponse.reviews()).hasSize(1);
            softly.assertThat(myReviewSliceResponse.reviews().get(0).review().id()).isEqualTo(review.getId());
        });
    }
}