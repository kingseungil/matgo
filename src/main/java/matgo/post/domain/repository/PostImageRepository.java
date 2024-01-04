package matgo.post.domain.repository;

import java.util.List;
import matgo.post.domain.entity.Post;
import matgo.post.domain.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    
    List<PostImage> findAllByPost(Post post);
}
