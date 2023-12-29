package matgo.review.domain.repository;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import matgo.member.dto.response.MemberResponse;
import matgo.restaurant.dto.response.RestaurantResponse;
import matgo.review.domain.entity.QReview;
import matgo.review.dto.response.ReviewResponse;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final QReview qReview = QReview.review;

    public Optional<ReviewResponse> findReviewResponseByIdWithMemberAndRestaurant(Long reviewId) {
        ReviewResponse reviewResponse = jpaQueryFactory.select(reviewProjection())
                                                       .from(qReview)
                                                       .join(qReview.member)
                                                       .join(qReview.restaurant)
                                                       .where(qReview.id.eq(reviewId))
                                                       .fetchOne();

        return Optional.ofNullable(reviewResponse);
    }

    private ConstructorExpression<ReviewResponse> reviewProjection() {
        return Projections.constructor(ReviewResponse.class,
          qReview.id,
          qReview.content,
          qReview.rating,
          qReview.imageUrl,
          qReview.revisit,
          memberProjection(),
          restaurantProjection());
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
}
