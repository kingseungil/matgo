package matgo.restaurant.domain.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import matgo.restaurant.domain.entity.QRestaurant;
import matgo.restaurant.dto.request.RestaurantRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RestaurantQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QRestaurant qRestaurant = QRestaurant.restaurant;

    public boolean existsByDuplicateField(RestaurantRequest request) {
        return queryFactory.selectOne()
                           .from(qRestaurant)
                           .where(
                             eqRoadAddress(request.roadAddress())
                               .or(eqAddress(request.address()))
                           )
                           .fetchFirst() != null;
    }

    private BooleanExpression eqRoadAddress(String roadAddress) {
        if (roadAddress != null) {
            return qRestaurant.roadAddress.eq(roadAddress);
        }

        return null;
    }

    private BooleanExpression eqAddress(String address) {
        if (address != null) {
            return qRestaurant.address.eq(address);
        }

        return null;
    }
}
