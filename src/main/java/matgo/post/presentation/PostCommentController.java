package matgo.post.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import matgo.auth.security.OnlyUser;
import matgo.post.application.PostCommentService;
import matgo.post.dto.request.PostCommentCreateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
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
      @RequestBody @Valid PostCommentCreateRequest postCommentCreateRequest
    ) {
        postCommentService.updateComment(Long.parseLong(userDetails.getUsername()), commentId,
          postCommentCreateRequest);
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

}
