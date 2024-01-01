package matgo.review.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import matgo.auth.security.BothAdminAndUser;
import matgo.auth.security.OnlyUser;
import matgo.global.type.Reaction;
import matgo.review.application.ReviewService;
import matgo.review.dto.request.ReviewCreateRequest;
import matgo.review.dto.response.ReviewCreateResponse;
import matgo.review.dto.response.ReviewResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 작성
    @PostMapping("/{restaurantId}")
    @OnlyUser
    public ResponseEntity<Void> createReview(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long restaurantId,
      @Valid @RequestPart ReviewCreateRequest reviewCreateRequest,
      @RequestPart(required = false) MultipartFile reviewImage
    ) {
        ReviewCreateResponse reviewCreateResponse = reviewService.createReview(
          Long.parseLong(userDetails.getUsername()), restaurantId, reviewCreateRequest, reviewImage);
        return ResponseEntity.created(URI.create("/api/reviews/detail/" + reviewCreateResponse.reviewId())).build();
    }

    // 리뷰 좋아요/싫어요 기능
    @PostMapping("/{reviewId}/reactions")
    @OnlyUser
    public ResponseEntity<Void> addReviewReaction(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long reviewId,
      @RequestParam Reaction reactionType
    ) {
        reviewService.addReviewReaction(Long.parseLong(userDetails.getUsername()), reviewId, reactionType);
        return ResponseEntity.noContent().build();
    }

    // 리뷰 조회 (페이징)

    // 리뷰 상세 보기
    @GetMapping("/detail/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(
      @PathVariable Long reviewId
    ) {
        ReviewResponse response = reviewService.getReviewDetail(reviewId);
        return ResponseEntity.ok().body(response);
    }

    // 리뷰 삭제
    @DeleteMapping("/{restaurantId}/{reviewId}")
    @BothAdminAndUser
    public ResponseEntity<Void> deleteReview(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long restaurantId,
      @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(Long.parseLong(userDetails.getUsername()), restaurantId, reviewId);
        return ResponseEntity.noContent().build();
    }

}
