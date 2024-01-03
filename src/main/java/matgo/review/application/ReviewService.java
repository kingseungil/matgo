package matgo.review.application;

import static matgo.global.exception.ErrorCode.ALREADY_WRITTEN_REVIEW;
import static matgo.global.exception.ErrorCode.NOT_FOUND_MEMBER;
import static matgo.global.exception.ErrorCode.NOT_FOUND_RESTAURANT;
import static matgo.global.exception.ErrorCode.NOT_FOUND_REVIEW;
import static matgo.global.exception.ErrorCode.NOT_FOUND_REVIEW_REACTION;
import static matgo.global.exception.ErrorCode.NOT_OWNER_REVIEW;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matgo.global.filesystem.s3.S3Service;
import matgo.global.lock.annotation.DistributedLock;
import matgo.global.type.Reaction;
import matgo.global.type.S3Directory;
import matgo.member.domain.entity.Member;
import matgo.member.domain.repository.MemberRepository;
import matgo.member.exception.MemberException;
import matgo.restaurant.domain.entity.Restaurant;
import matgo.restaurant.domain.repository.RestaurantRepository;
import matgo.restaurant.domain.repository.RestaurantSearchRepositoryImpl;
import matgo.restaurant.exception.RestaurantException;
import matgo.review.domain.entity.Review;
import matgo.review.domain.entity.ReviewReaction;
import matgo.review.domain.repository.ReviewQueryRepository;
import matgo.review.domain.repository.ReviewReactionRepository;
import matgo.review.domain.repository.ReviewRepository;
import matgo.review.dto.request.ReviewCreateRequest;
import matgo.review.dto.response.ReviewCreateResponse;
import matgo.review.dto.response.ReviewResponse;
import matgo.review.dto.response.ReviewSliceResponse;
import matgo.review.exception.ReviewException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {


    private final ReviewRepository reviewRepository;
    private final ReviewQueryRepository reviewQueryRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantSearchRepositoryImpl restaurantSearchRepositoryImpl;
    private final MemberRepository memberRepository;
    private final ReviewReactionRepository reviewReactionRepository;

    private final S3Service s3Service;


    @DistributedLock(key = "'createReview-' + #restaurantId")
    public ReviewCreateResponse createReview(Long memberId, Long restaurantId,
      ReviewCreateRequest reviewCreateRequest, MultipartFile reviewImage) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                                                    .orElseThrow(() -> new RestaurantException(NOT_FOUND_RESTAURANT));

        checkCanWriteReview(memberId, restaurant);
        Member member = memberRepository.findById(memberId)
                                        .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));

        String imageUrl = s3Service.uploadAndGetImageURL(reviewImage, S3Directory.REVIEW);
        Review review = Review.from(member, restaurant, reviewCreateRequest, imageUrl);
        restaurant.addReview(review);
        member.addReview(review);
        reviewRepository.save(review);

        updateRestaurantInfoInES(restaurantId, restaurant);

        return new ReviewCreateResponse(review.getId());
    }

    private void updateRestaurantInfoInES(Long restaurantId, Restaurant restaurant) {
        // ES에 실시간 동기화
        restaurantSearchRepositoryImpl.updateRatingAndReviewCount(String.valueOf(restaurantId), restaurant.getRating(),
          restaurant.getReviewCount());
        log.info("Updated restaurant info in ES. restaurantId: {}, rating: {}, reviewCount: {}", restaurantId,
          restaurant.getRating(), restaurant.getReviewCount());
    }

    private void checkCanWriteReview(Long memberId, Restaurant restaurant) {
        if (reviewQueryRepository.existsByMemberIdAndRestaurantId(memberId, restaurant.getId())) {
            throw new ReviewException(ALREADY_WRITTEN_REVIEW);
        }
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewDetail(Long reviewId) {
        return reviewQueryRepository.findReviewResponseByIdWithMemberAndRestaurant(reviewId)
                                    .orElseThrow(() -> new ReviewException(NOT_FOUND_REVIEW));
    }

    @Transactional(readOnly = true)
    public ReviewSliceResponse getReviews(Long restaurantId, Pageable pageable) {
        return reviewQueryRepository.findAllReviewSliceByRestaurantId(restaurantId, pageable);
    }

    @DistributedLock(key = "'addReviewReaction-' + #reviewId")
    public void addReviewReaction(Long memberId, Long reviewId, Reaction reactionType) {
        Review review = reviewRepository.findById(reviewId)
                                        .orElseThrow(() -> new ReviewException(NOT_FOUND_REVIEW));
        Member member = memberRepository.findById(memberId)
                                        .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));

        // 이미 반응이 있다면 기존 반응 삭제 후 새로운 반응으로 업데이트
        if (review.hasReaction(member)) {
            updateReaction(review, member, reactionType);
        } else {
            addReaction(review, member, reactionType);
        }

        reviewRepository.save(review);
    }

    private void updateReaction(Review review, Member member, Reaction reaction) {
        ReviewReaction reviewReaction = review.getReviewReactions()
                                              .stream()
                                              .filter(r -> r.getMember().getId().equals(member.getId()))
                                              .findFirst()
                                              .orElseThrow(
                                                () -> new ReviewException(NOT_FOUND_REVIEW_REACTION));

        // 기존 반응과 같다면 반응 삭제 (아무것도 안누른 상태로 변경)
        if (reviewReaction.getReaction() == reaction) {
            reviewReactionRepository.delete(reviewReaction);
            review.removeReviewReaction(reviewReaction);
            if (reaction == Reaction.LIKE) {
                review.decreaseLikeCount();
            } else {
                review.decreaseDislikeCount();
            }
        } else { // 기존 반응과 다르다면 반응 업데이트
            reviewReaction.changeReaction(reaction);
            if (reaction == Reaction.LIKE) {
                review.increaseLikeCount();
                review.decreaseDislikeCount();
            } else {
                review.increaseDislikeCount();
                review.decreaseLikeCount();
            }
        }
    }

    private void addReaction(Review review, Member member, Reaction reaction) {
        ReviewReaction reviewReaction = ReviewReaction.from(review, member, reaction);
        review.addReviewReaction(reviewReaction);
        reviewReactionRepository.save(reviewReaction);
        if (reaction == Reaction.LIKE) {
            review.increaseLikeCount();
        } else {
            review.increaseDislikeCount();
        }
    }

    @DistributedLock(key = "'deleteReview-' + #reviewId")
    public void deleteReview(Long memberId, Long restaurantId, Long reviewId) {
        Review review = reviewQueryRepository.findByIdWithReactions(reviewId, restaurantId)
                                             .orElseThrow(() -> new ReviewException(NOT_FOUND_REVIEW));
        Member member = memberRepository.findById(memberId)
                                        .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
        checkCanDeleteReview(member, review);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                                                    .orElseThrow(() -> new RestaurantException(NOT_FOUND_RESTAURANT));

        restaurant.removeReview(review);
        member.removeReview(review);
        reviewRepository.delete(review);

        updateRestaurantInfoInES(restaurantId, restaurant);
    }

    private void checkCanDeleteReview(Member member, Review review) {
        if (!reviewRepository.existsByIdAndMemberId(review.getId(), member.getId())) {
            throw new ReviewException(NOT_OWNER_REVIEW);
        }
    }
}