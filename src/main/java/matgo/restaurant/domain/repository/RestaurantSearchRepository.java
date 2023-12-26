package matgo.restaurant.domain.repository;

import matgo.restaurant.domain.entity.RestaurantSearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface RestaurantSearchRepository extends ElasticsearchRepository<RestaurantSearch, Long> {

}
