package matgo.review.domain.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import matgo.review.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByIdAndMemberId(Long reviewId, Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Review r where r.id = :reviewId")
    Optional<Review> findByIdWithPessimisticLock(Long reviewId);
}