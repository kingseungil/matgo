package matgo.restaurant.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import matgo.restaurant.domain.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByModifiedAtAfter(LocalDateTime time);
}
