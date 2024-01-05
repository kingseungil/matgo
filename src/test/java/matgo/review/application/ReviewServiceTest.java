package matgo.review.application;

import static matgo.global.exception.ErrorCode.ALREADY_WRITTEN_REVIEW;
import static matgo.global.exception.ErrorCode.NOT_FOUND_RESTAURANT;
import static matgo.global.exception.ErrorCode.NOT_FOUND_REVIEW;
import static matgo.global.exception.ErrorCode.NOT_OWNER_REVIEW;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import matgo.common.BaseServiceTest;
import matgo.global.type.Reaction;
import matgo.global.type.S3Directory;
import matgo.member.domain.entity.Member;
import matgo.member.domain.entity.Region;
import matgo.member.domain.type.UserRole;
import matgo.member.dto.response.MemberResponse;
import matgo.restaurant.domain.entity.Restaurant;
import matgo.restaurant.dto.response.RestaurantResponse;
import matgo.restaurant.exception.RestaurantException;
import matgo.review.domain.entity.Review;
import matgo.review.domain.entity.ReviewReaction;
import matgo.review.dto.request.ReviewCreateRequest;
import matgo.review.dto.response.ReviewCreateResponse;
import matgo.review.dto.response.ReviewResponse;
import matgo.review.dto.response.ReviewSliceResponse;
import matgo.review.exception.ReviewException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class ReviewServiceTest extends BaseServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    private Member member;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        Region region = new Region("효자동");
        member = Member.builder()
                       .id(1L)
                       .email("test@naver.com")
                       .nickname("testnick")
                       .password("!1asdasd")
                       .profileImage(
                         "https://matgo-bucket.s3.ap-northeast-2.amazonaws.com/matgo/member/default_image")
                       .role(UserRole.ROLE_USER)
                       .region(region)
                       .isActive(true)
                       .reviews(new ArrayList<>())
                       .build();
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
                               .reviews(new ArrayList<>())
                               .build();
        restaurantRepository.save(restaurant);
    }

    @Nested
    @DisplayName("createReview 메서드는")
    class CreateReviewTest {

        ReviewCreateRequest reviewCreateRequest = new ReviewCreateRequest("맛있어요", 5, true);
        MultipartFile reviewImage = new MockMultipartFile("profileImage", "img.jpeg", "image/jpeg",
          "image data".getBytes(StandardCharsets.UTF_8));

        @Test
        @DisplayName("리뷰 작성에 성공하면 ReviewCreateResponse를 반환한다.")
        void createReviewSuccess() {
            // given
            doReturn(Optional.of(restaurant)).when(restaurantRepository)
                                             .findById(anyLong());
            doReturn(false).when(reviewQueryRepository).existsByMemberIdAndRestaurantId(anyLong(), anyLong());
            doReturn(Optional.of(member)).when(memberRepository).findById(anyLong());
            doReturn("mocked_url").when(s3Service)
                                  .uploadAndGetImageURL(any(MultipartFile.class), eq(S3Directory.REVIEW));
            doNothing().when(restaurantSearchRepositoryImpl)
                       .updateRatingAndReviewCount(any(String.class), any(Double.class), any(Integer.class));
            Review review = ReviewCreateRequest.toEntity(member, restaurant, reviewCreateRequest, "mocked_url");
            doReturn(review).when(reviewRepository).save(any(Review.class));

            // when
            ReviewCreateResponse reviewCreateResponse = reviewService.createReview(member.getId(), restaurant.getId(),
              reviewCreateRequest, reviewImage);

            // then
            assertSoftly(softly -> {
                softly.assertThat(restaurant.getRating()).isEqualTo(5.0);
                softly.assertThat(restaurant.getReviewCount()).isEqualTo(1);
                softly.assertThat(reviewCreateResponse.reviewId()).isEqualTo(review.getId());
            });
        }

        @Test
        @DisplayName("restaurantId에 해당하는 식당이 없으면 RestaurantException을 던진다.")
        void createReviewFailBecauseNotFoundRestaurant() {
            // given
            doReturn(Optional.empty()).when(restaurantRepository).findById(anyLong());

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(member.getId(), 1L, reviewCreateRequest, reviewImage))
              .isInstanceOf(RestaurantException.class)
              .hasMessageContaining(NOT_FOUND_RESTAURANT.getMessage());
        }

        @Test
        @DisplayName("이미 리뷰를 작성한 식당에 대해 리뷰를 작성하면 ReviewException을 던진다.")
        void createReviewFailBecauseAlreadyWrittenReview() {
            // given
            doReturn(Optional.of(restaurant)).when(restaurantRepository)
                                             .findById(anyLong());
            doReturn(true).when(reviewQueryRepository).existsByMemberIdAndRestaurantId(anyLong(), anyLong());

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(member.getId(), restaurant.getId(), reviewCreateRequest,
              reviewImage))
              .isInstanceOf(ReviewException.class)
              .hasMessageContaining(ALREADY_WRITTEN_REVIEW.getMessage());
        }
    }

    @Nested
    @DisplayName("리뷰 조회 메서드")
    class GetReviewTest {


        MemberResponse memberResponse = new MemberResponse(1L, "image_url", "nickname");
        RestaurantResponse restaurantResponse = new RestaurantResponse(1L, "식당", "서울시 강남구 테헤란로 427");

        ReviewResponse reviewResponse = new ReviewResponse(1L, "맛있어요", 5, "mocked_url", true, 0, 0,
          LocalDateTime.now(), memberResponse, restaurantResponse);
        ReviewResponse reviewResponse2 = new ReviewResponse(2L, "맛있어요", 5, "mocked_url", true, 0, 0,
          LocalDateTime.now(), memberResponse, restaurantResponse);

        @Test
        @DisplayName("리뷰 상세 조회에 성공하면 ReviewResponse를 반환한다.")
        void getReviewDetailSuccess() {
            // given
            doReturn(Optional.of(reviewResponse)).when(reviewQueryRepository)
                                                 .findReviewResponseByIdWithMemberAndRestaurant(anyLong());

            // when
            ReviewResponse reviewResponse = reviewService.getReviewDetail(1L);

            // then
            assertSoftly(softly -> {
                softly.assertThat(reviewResponse.reviewId()).isEqualTo(1L);
                softly.assertThat(reviewResponse.content()).isEqualTo("맛있어요");
                softly.assertThat(reviewResponse.rating()).isEqualTo(5);
                softly.assertThat(reviewResponse.imageUrl()).isEqualTo("mocked_url");
                softly.assertThat(reviewResponse.likeCount()).isEqualTo(0);
                softly.assertThat(reviewResponse.createdAt()).isNotNull();
                softly.assertThat(reviewResponse.member().id()).isEqualTo(1L);
                softly.assertThat(reviewResponse.member().profileImage()).isEqualTo("image_url");
                softly.assertThat(reviewResponse.member().nickname()).isEqualTo("nickname");
                softly.assertThat(reviewResponse.restaurant().restaurantId()).isEqualTo(1L);
                softly.assertThat(reviewResponse.restaurant().name()).isEqualTo("식당");
                softly.assertThat(reviewResponse.restaurant().roadAddress()).isEqualTo("서울시 강남구 테헤란로 427");
            });

        }

        @Test
        @DisplayName("리뷰 상세 조회에 실패하면 ReviewException을 던진다.")
        void getReviewDetailFail() {
            // given
            doReturn(Optional.empty()).when(reviewQueryRepository).findReviewResponseByIdWithMemberAndRestaurant(
              anyLong());

            // when & then
            assertThatThrownBy(() -> reviewService.getReviewDetail(1L))
              .isInstanceOf(ReviewException.class)
              .hasMessageContaining(NOT_FOUND_REVIEW.getMessage());
        }

        @Test
        @DisplayName("식당에 대한 리뷰 목록 조회에 성공하면 ReviewSliceResponse를 반환한다.")
        void getReviewsSuccess() {
            // given
            PageRequest pageRequest = PageRequest.of(0, 2);
            List<ReviewResponse> reviewResponses = Arrays.asList(reviewResponse, reviewResponse2);
            ReviewSliceResponse reviewSliceResponse = new ReviewSliceResponse(reviewResponses, false);

            doReturn(reviewSliceResponse).when(reviewQueryRepository).findAllReviewSliceByRestaurantId(anyLong(),
              any(PageRequest.class));

            // when
            ReviewSliceResponse reviews = reviewService.getReviews(1L, pageRequest);

            // then
            assertSoftly(softly -> {
                softly.assertThat(reviews.reviews().size()).isEqualTo(2);
                softly.assertThat(reviews.hasNext()).isFalse();
            });
        }
    }

    @Nested
    @DisplayName("addReviewReaction 메서드는")
    class AddReviewReactionTest {

        Review review = Review.builder()
                              .id(1L)
                              .content("맛있어요")
                              .rating(5)
                              .imageUrl("mocked_url")
                              .likeCount(0)
                              .dislikeCount(0)
                              .restaurant(restaurant)
                              .reviewReactions(new ArrayList<>())
                              .build();

        @Test
        @DisplayName("리뷰에 좋아요 누르면 리뷰의 좋아요 수를 1 증가시킨다.")
        void addReviewReactionSuccess() {
            // given
            doReturn(Optional.of(review)).when(reviewRepository).findById(anyLong());
            doReturn(Optional.of(member)).when(memberRepository).findById(anyLong());
            doReturn(null).when(reviewRepository).save(any(Review.class));
            doReturn(null).when(reviewReactionRepository).save(any());

            // when
            reviewService.addReviewReaction(member.getId(), review.getId(), Reaction.LIKE);

            // then
            assertSoftly(softly -> {
                softly.assertThat(review.getLikeCount()).isEqualTo(1);
                softly.assertThat(review.getDislikeCount()).isEqualTo(0);
            });
        }

        @Test
        @DisplayName("이미 좋아요 누른 상태에서 다시 좋아요 누르면 리뷰의 좋아요 수를 1 감소시킨다.")
        void addReviewReactionSuccess2() {
            // given
            ReviewReaction reviewReaction = ReviewReaction.builder()
                                                          .member(member)
                                                          .review(review)
                                                          .reaction(Reaction.LIKE)
                                                          .build();
            review.addReviewReaction(reviewReaction);
            review.increaseLikeCount();

            doReturn(Optional.of(review)).when(reviewRepository).findById(anyLong());
            doReturn(Optional.of(member)).when(memberRepository).findById(anyLong());
            doReturn(null).when(reviewRepository).save(any(Review.class));
            doNothing().when(reviewReactionRepository).delete(any());

            // when
            reviewService.addReviewReaction(member.getId(), review.getId(), Reaction.LIKE);

            // then
            assertSoftly(softly -> {
                softly.assertThat(review.getLikeCount()).isEqualTo(0);
                softly.assertThat(review.getDislikeCount()).isEqualTo(0);
            });
        }

        @Test
        @DisplayName("이미 좋아요 누른 상태에서 싫어요를 누르면 리뷰의 좋아요 수를 1 감소시키고 싫어요 수를 1 증가시킨다.")
        void addReviewReactionSuccess3() {
            // given
            ReviewReaction reviewReaction = ReviewReaction.builder()
                                                          .member(member)
                                                          .review(review)
                                                          .reaction(Reaction.LIKE)
                                                          .build();
            review.addReviewReaction(reviewReaction);
            review.increaseLikeCount();

            doReturn(Optional.of(review)).when(reviewRepository).findById(anyLong());
            doReturn(Optional.of(member)).when(memberRepository).findById(anyLong());
            doReturn(null).when(reviewRepository).save(any(Review.class));

            // when
            reviewService.addReviewReaction(member.getId(), review.getId(), Reaction.DISLIKE);

            // then
            assertSoftly(softly -> {
                softly.assertThat(review.getLikeCount()).isEqualTo(0);
                softly.assertThat(review.getDislikeCount()).isEqualTo(1);
                softly.assertThat(reviewReaction.getReaction()).isEqualTo(Reaction.DISLIKE);
            });
        }
    }

    @Nested
    @DisplayName("deleteReview 메서드는")
    class DeleteReviewTest {

        Review review = Review.builder()
                              .id(1L)
                              .content("맛있어요")
                              .rating(5)
                              .imageUrl("mocked_url")
                              .likeCount(0)
                              .dislikeCount(0)
                              .restaurant(restaurant)
                              .member(member)
                              .reviewReactions(new ArrayList<>())
                              .build();


        @Test
        @DisplayName("리뷰 삭제에 성공하면 restaurant의 rating과 reviewCount를 업데이트한다.")
        void deleteReviewSuccess() {
            // given
            restaurant.addReview(review);
            doReturn(Optional.of(review)).when(reviewQueryRepository).findByIdWithReactions(anyLong(), anyLong());
            doReturn(Optional.of(member)).when(memberRepository).findById(anyLong());
            doReturn(true).when(reviewRepository).existsByIdAndMemberId(anyLong(), anyLong());
            doReturn(Optional.of(restaurant)).when(restaurantRepository).findById(anyLong());
            doNothing().when(reviewRepository).delete(any(Review.class));
            doNothing().when(restaurantSearchRepositoryImpl)
                       .updateRatingAndReviewCount(any(String.class), any(Double.class), any(Integer.class));

            // when
            reviewService.deleteReview(member.getId(), restaurant.getId(), review.getId());

            // then
            assertSoftly(softly -> {
                softly.assertThat(restaurant.getRating()).isEqualTo(0.0);
                softly.assertThat(restaurant.getReviewCount()).isEqualTo(0);
            });
        }

        @Test
        @DisplayName("review가 존재하지 않으면 ReviewException을 던진다.")
        void deleteReviewFailBecauseNotFoundReview() {
            // given
            doReturn(Optional.empty()).when(reviewQueryRepository).findByIdWithReactions(anyLong(), anyLong());

            // when & then
            assertThatThrownBy(() -> reviewService.deleteReview(member.getId(), restaurant.getId(), review.getId()))
              .isInstanceOf(ReviewException.class)
              .hasMessageContaining(NOT_FOUND_REVIEW.getMessage());
        }

        @Test
        @DisplayName("리뷰 작성자가 아니면 ReviewException을 던진다.")
        void deleteReviewFailBecauseNotWriter() {
            // given
            doReturn(Optional.of(review)).when(reviewQueryRepository).findByIdWithReactions(anyLong(), anyLong());
            doReturn(Optional.of(member)).when(memberRepository).findById(anyLong());
            doReturn(false).when(reviewRepository).existsByIdAndMemberId(anyLong(), anyLong());

            // when & then
            assertThatThrownBy(() -> reviewService.deleteReview(member.getId(), restaurant.getId(), review.getId()))
              .isInstanceOf(ReviewException.class)
              .hasMessageContaining(NOT_OWNER_REVIEW.getMessage());
        }
    }
}