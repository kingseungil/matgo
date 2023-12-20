package matgo.auth.domain.repository;

import java.util.Optional;
import matgo.auth.domain.entity.Token;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends CrudRepository<Token, Long> {

    Optional<Token> findByAccessToken(String accessToken);
}
