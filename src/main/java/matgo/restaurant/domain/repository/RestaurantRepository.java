package matgo.restaurant.domain.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import matgo.restaurant.domain.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByModifiedAtAfterAndApprovedAtIsNotNull(LocalDateTime modifiedAt);

    @Query("SELECT r FROM Restaurant r WHERE r.externalId IN :externalIds")
    List<Restaurant> findByExternalIdIn(List<String> externalIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Restaurant r WHERE r.id = :id")
    Optional<Restaurant> findByIdWithPessimisticWriteLock(Long id);
}
