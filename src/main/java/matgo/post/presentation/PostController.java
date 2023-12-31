package matgo.post.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matgo.auth.security.OnlyUser;
import matgo.global.type.Reaction;
import matgo.post.application.PostService;
import matgo.post.dto.request.PostCreateRequest;
import matgo.post.dto.request.PostUpdateRequest;
import matgo.post.dto.response.MyPostSliceResponse;
import matgo.post.dto.response.PostCreateResponse;
import matgo.post.dto.response.PostDetailResponse;
import matgo.post.dto.response.PostSliceResponse;
import matgo.restaurant.dto.request.CustomPageRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        return ResponseEntity.created(URI.create("/api/posts/detail/" + postCreateResponse.postId())).build();
    }

    // 게시글 수정
    @PutMapping("/{postId}")
    @OnlyUser
    public ResponseEntity<Void> updatePost(
      @PathVariable Long postId,
      @AuthenticationPrincipal UserDetails userDetails,
      @Valid @RequestPart PostUpdateRequest postUpdateRequest,
      @RequestPart(required = false) List<MultipartFile> postImages
    ) {
        postService.updatePost(postId, Long.parseLong(userDetails.getUsername()), postUpdateRequest, postImages);
        return ResponseEntity.noContent().build();
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    @OnlyUser
    public ResponseEntity<Void> deletePost(
      @PathVariable Long postId,
      @AuthenticationPrincipal UserDetails userDetails
    ) {
        postService.deletePost(postId, Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.noContent().build();
    }

    // 게시글 상세 조회
    @GetMapping("/detail/{postId}")
    @OnlyUser
    public ResponseEntity<PostDetailResponse> getPostDetail(
      @PathVariable Long postId,
      @AuthenticationPrincipal UserDetails userDetails

    ) {
        PostDetailResponse postDetail = postService.getPostDetailByRegion(
          Long.parseLong(userDetails.getUsername()), postId);
        return ResponseEntity.ok().body(postDetail);
    }

    // 게시글 목록 조회 (페이징)
    @GetMapping
    @OnlyUser
    public ResponseEntity<PostSliceResponse> getPosts(
      @Valid CustomPageRequest customPageRequest,
      @AuthenticationPrincipal UserDetails userDetails
    ) {
        Pageable pageable = PageRequest.of(
          customPageRequest.page(),
          customPageRequest.size(),
          customPageRequest.getSort()
        );

        PostSliceResponse response = postService.getPostsByRegion(Long.parseLong(userDetails.getUsername()), pageable);
        return ResponseEntity.ok().body(response);
    }

    // 게시글 좋아요/싫어요
    @PostMapping("/{postId}/reactions")
    @OnlyUser
    public ResponseEntity<Void> addPostReaction(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long postId,
      @RequestParam Reaction reactionType
    ) {
        postService.addPostReaction(Long.parseLong(userDetails.getUsername()), postId, reactionType);
        return ResponseEntity.noContent().build();
    }

    // 내가 작성한 게시글 조회
    @GetMapping("/my/writable-posts")
    @OnlyUser
    public ResponseEntity<MyPostSliceResponse> getMyPosts(
      @AuthenticationPrincipal UserDetails userDetails,
      @Valid CustomPageRequest customPageRequest
    ) {
        Pageable pageable = PageRequest.of(
          customPageRequest.page(),
          customPageRequest.size(),
          customPageRequest.getSort()
        );

        MyPostSliceResponse response = postService.getMyPosts(Long.parseLong(userDetails.getUsername()), pageable);
        return ResponseEntity.ok().body(response);
    }
}
