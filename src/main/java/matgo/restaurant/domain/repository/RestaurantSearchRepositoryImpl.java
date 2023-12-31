package matgo.restaurant.domain.repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import matgo.restaurant.domain.entity.RestaurantSearch;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RestaurantSearchRepositoryImpl {

    private final ElasticsearchOperations operations;

    public void bulkInsertOrUpdate(List<RestaurantSearch> restaurantSearches) {
        List<UpdateQuery> updates = restaurantSearches.stream().map(restaurantSearch ->
          UpdateQuery.builder(Objects.requireNonNull(restaurantSearch.getId()))
                     .withDocument(operations.getElasticsearchConverter().mapObject(restaurantSearch))
                     .withDocAsUpsert(true) // 없으면 insert, 있으면 update
                     .build()).toList();

        operations.bulkUpdate(updates, operations.getIndexCoordinatesFor(RestaurantSearch.class));
    }

    public void updateRatingAndReviewCount(String id, double rating, int reviewCount) {
        Map<String, Object> updates = Map.of("rating", rating, "reviewCount", reviewCount);
        UpdateQuery updateQuery = UpdateQuery.builder(id)
                                             .withDocument(operations.getElasticsearchConverter().mapObject(updates))
                                             .build();

        operations.update(updateQuery, operations.getIndexCoordinatesFor(RestaurantSearch.class));
    }
}
