package matgo.review.domain.repository;

import matgo.review.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByMemberIdAndRestaurantId(Long memberId, Long restaurantId);

}