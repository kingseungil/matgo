package matgo.review.domain.repository;

import matgo.review.domain.entity.ReviewReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewReactionRepository extends JpaRepository<ReviewReaction, Long> {

}
