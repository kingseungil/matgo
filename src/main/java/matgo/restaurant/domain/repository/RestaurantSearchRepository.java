package matgo.restaurant.domain.repository;

import matgo.restaurant.domain.entity.RestaurantSearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantSearchRepository extends ElasticsearchRepository<RestaurantSearch, String> {

}
