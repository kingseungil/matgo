package matgo.review.domain.repository;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import matgo.member.dto.response.MemberResponse;
import matgo.restaurant.dto.response.RestaurantResponse;
import matgo.review.domain.entity.QReview;
import matgo.review.domain.entity.Review;
import matgo.review.dto.response.MyReviewResponse;
import matgo.review.dto.response.MyReviewSliceResponse;
import matgo.review.dto.response.ReviewDetailResponse;
import matgo.review.dto.response.ReviewResponse;
import matgo.review.dto.response.ReviewSliceResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final QReview qReview = QReview.review;

    public Optional<ReviewDetailResponse> findReviewResponseByIdWithMemberAndRestaurant(Long reviewId) {
        ReviewDetailResponse response = jpaQueryFactory.select(reviewDetailProjection())
                                                       .from(qReview)
                                                       .join(qReview.member)
                                                       .join(qReview.restaurant)
                                                       .where(qReview.id.eq(reviewId))
                                                       .fetchOne();

        return Optional.ofNullable(response);
    }

    public ReviewSliceResponse findAllReviewSliceByRestaurantId(Long restaurantId, Pageable pageable) {
        List<ReviewDetailResponse> responses = jpaQueryFactory.select(reviewDetailProjection())
                                                              .from(qReview)
                                                              .join(qReview.member)
                                                              .join(qReview.restaurant)
                                                              .where(qReview.restaurant.id.eq(restaurantId))
                                                              .orderBy(getOrderSpecifier(pageable.getSort()))
                                                              .offset(pageable.getOffset())
                                                              .limit(pageable.getPageSize())
                                                              .fetch();

        return new ReviewSliceResponse(responses, responses.size() == pageable.getPageSize());
    }

    private OrderSpecifier<?>[] getOrderSpecifier(Sort sort) {
        return sort.stream()
                   .map(order -> switch (order.getProperty()) {
                       case "rating" ->
                         order.isAscending() ? QReview.review.rating.asc() : QReview.review.rating.desc();
                       case "createdAt" ->
                         order.isAscending() ? QReview.review.createdAt.asc() : QReview.review.createdAt.desc();
                       case "likeCount" ->
                         order.isAscending() ? QReview.review.likeCount.asc() : QReview.review.likeCount.desc();
                       case "dislikeCount" -> order.isAscending() ? QReview.review.dislikeCount.asc()
                         : QReview.review.dislikeCount.desc();
                       default -> QReview.review.createdAt.desc();
                   })
                   .toArray(OrderSpecifier[]::new);
    }

    private ConstructorExpression<ReviewDetailResponse> reviewDetailProjection() {
        return Projections.constructor(ReviewDetailResponse.class,
          reviewProjection(),
          memberProjection(),
          restaurantProjection());
    }

    private ConstructorExpression<ReviewResponse> reviewProjection() {
        return Projections.constructor(ReviewResponse.class,
          qReview.id,
          qReview.content,
          qReview.rating,
          qReview.imageUrl,
          qReview.revisit,
          qReview.likeCount,
          qReview.dislikeCount,
          qReview.createdAt
        );
    }

    private ConstructorExpression<MemberResponse> memberProjection() {
        return Projections.constructor(MemberResponse.class,
          qReview.member.id,
          qReview.member.profileImage,
          qReview.member.nickname);
    }

    private ConstructorExpression<RestaurantResponse> restaurantProjection() {
        return Projections.constructor(RestaurantResponse.class,
          qReview.restaurant.id,
          qReview.restaurant.name,
          qReview.restaurant.roadAddress);
    }

    public Boolean existsByMemberIdAndRestaurantId(Long memberId, Long restaurantId) {
        return jpaQueryFactory.selectOne()
                              .from(qReview)
                              .where(qReview.member.id.eq(memberId),
                                qReview.restaurant.id.eq(restaurantId))
                              .fetchFirst() != null;
    }

    public Optional<Review> findByIdWithReactions(Long reviewId, Long restaurantId) {
        return Optional.ofNullable(jpaQueryFactory.selectFrom(qReview)
                                                  .leftJoin(qReview.reviewReactions).fetchJoin()
                                                  .where(qReview.id.eq(reviewId),
                                                    qReview.restaurant.id.eq(restaurantId))
                                                  .fetchOne());
    }

    public MyReviewSliceResponse findAllReviewSliceByMemberId(Long memberId, Pageable pageable) {
        List<MyReviewResponse> responses = jpaQueryFactory.select(myReviewProjection())
                                                          .from(qReview)
                                                          .join(qReview.restaurant)
                                                          .where(qReview.member.id.eq(memberId))
                                                          .orderBy(getOrderSpecifier(pageable.getSort()))
                                                          .offset(pageable.getOffset())
                                                          .limit(pageable.getPageSize())
                                                          .fetch();

        return new MyReviewSliceResponse(responses, responses.size() == pageable.getPageSize());
    }

    private ConstructorExpression<MyReviewResponse> myReviewProjection() {
        return Projections.constructor(MyReviewResponse.class,
          reviewProjection(),
          restaurantProjection());
    }
}

