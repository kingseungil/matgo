package matgo.post.domain.repository;

import matgo.post.domain.entity.PostReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {

}
