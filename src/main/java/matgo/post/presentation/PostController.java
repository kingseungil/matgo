package matgo.post.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matgo.auth.security.OnlyUser;
import matgo.post.application.PostService;
import matgo.post.dto.request.PostCreateRequest;
import matgo.post.dto.response.PostCreateResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    // 게시글 작성
    @PostMapping("/new")
    @OnlyUser
    public ResponseEntity<Void> createPost(
      @AuthenticationPrincipal UserDetails userDetails,
      @Valid @RequestPart PostCreateRequest postCreateRequest,
      @RequestPart(required = false) List<MultipartFile> postImages
    ) {
        PostCreateResponse postCreateResponse = postService.createPost(
          Long.parseLong(userDetails.getUsername()), postCreateRequest, postImages);
        return ResponseEntity.created(URI.create("/api/post/detail/" + postCreateResponse.postId())).build();
    }

    // 게시글 수정

    // 게시글 삭제

    // 게시글 조회

    // 게시글 좋아요/싫어요

}
