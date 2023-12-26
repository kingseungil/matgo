package matgo.restaurant.domain.repository;

import matgo.restaurant.domain.entity.RestaurantSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantSearchRepository extends ElasticsearchRepository<RestaurantSearch, String> {

    @Query("{\"match_phrase\": {\"address\": \"?0\"}}")
    Page<RestaurantSearch> findByAddressExactMatch(String addressKeyword, Pageable pageable);
}
