package matgo.post.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import matgo.auth.security.OnlyUser;
import matgo.post.application.PostCommentService;
import matgo.post.dto.request.PostCommentCreateRequest;
import matgo.post.dto.request.PostCommentUpdateRequest;
import matgo.post.dto.response.PostCommentSliceResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class PostCommentController {

    private final PostCommentService postCommentService;

    // 댓글 작성
    @PostMapping("/{postId}")
    @OnlyUser
    public ResponseEntity<Void> createComment(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long postId,
      @RequestBody @Valid PostCommentCreateRequest postCommentCreateRequest
    ) {
        postCommentService.createComment(Long.parseLong(userDetails.getUsername()), postId, postCommentCreateRequest);
        return ResponseEntity.ok().build();
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    @OnlyUser
    public ResponseEntity<Void> updateComment(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long commentId,
      @RequestBody @Valid PostCommentUpdateRequest postCommentUpdateRequest
    ) {
        postCommentService.updateComment(Long.parseLong(userDetails.getUsername()), commentId,
          postCommentUpdateRequest);
        return ResponseEntity.ok().build();
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    @OnlyUser
    public ResponseEntity<Void> deleteComment(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long commentId
    ) {
        postCommentService.deleteComment(Long.parseLong(userDetails.getUsername()), commentId);
        return ResponseEntity.ok().build();
    }

    // 내가 작성한 댓글 조회
    @GetMapping("/my/writable-comments")
    @OnlyUser
    public ResponseEntity<PostCommentSliceResponse> getMyWritableComments(
      @AuthenticationPrincipal UserDetails userDetails,
      @Valid CustomPageRequest customPageRequest
    ) {
        Pageable pageable = PageRequest.of(
          customPageRequest.page(),
          customPageRequest.size(),
          customPageRequest.getSort()
        );

        PostCommentSliceResponse postCommentSliceResponse = postCommentService.getMyComments(
          Long.parseLong(userDetails.getUsername()), pageable);
        return ResponseEntity.ok(postCommentSliceResponse);
    }

}
