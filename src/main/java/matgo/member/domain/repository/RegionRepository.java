package matgo.member.domain.repository;

import java.util.Optional;
import matgo.member.domain.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {


    Optional<Region> findByName(String region);
}
